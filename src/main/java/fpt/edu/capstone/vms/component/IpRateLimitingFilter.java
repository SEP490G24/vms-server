package fpt.edu.capstone.vms.component;

import fpt.edu.capstone.vms.util.IpRequestCounter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IpRateLimitingFilter extends OncePerRequestFilter {

    private Map<String, IpRequestCounter> ipRequestCounters = new ConcurrentHashMap<>();
    private final int maxCalls = 7;
    private final long timeWindowInMillis = 600000; // 2 minutes


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String clientIp = request.getRemoteAddr();
        IpRequestCounter counter = ipRequestCounters.get(clientIp);

        if (counter == null) {
            counter = new IpRequestCounter();
            ipRequestCounters.put(clientIp, counter);
        }

        long currentTime = System.currentTimeMillis();

        counter.cleanupOldRequests(currentTime - timeWindowInMillis);

        if (counter.getRequestCount() >= maxCalls) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("IP is blocked for 10 minutes.");
            return;
        }

        counter.incrementRequestCount(currentTime);
        filterChain.doFilter(request, response);
    }

}
