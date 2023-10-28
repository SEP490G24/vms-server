package fpt.edu.capstone.vms.component;

import fpt.edu.capstone.vms.util.IpRequestCounter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IpRateLimitingFilter extends GenericFilterBean {

    private Map<String, IpRequestCounter> ipRequestCounters = new ConcurrentHashMap<>();
    private final int maxCalls = 7;
    private final long timeWindowInMillis = 600000; // 2 minutes


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String requestUri = httpRequest.getRequestURI();

        if ("/api/v1/file/uploadImage".equals(requestUri)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getDetails() instanceof WebAuthenticationDetails) {
                String clientIp = ((WebAuthenticationDetails) authentication.getDetails()).getRemoteAddress();

                IpRequestCounter counter = ipRequestCounters.get(clientIp);

                if (counter == null) {
                    counter = new IpRequestCounter();
                    ipRequestCounters.put(clientIp, counter);
                }

                long currentTime = System.currentTimeMillis();

                counter.cleanupOldRequests(currentTime - timeWindowInMillis);

                if (counter.getRequestCount() >= maxCalls) {
                    servletResponse.getWriter().write("Ip is block 10 minutes.");
                    return;
                }

                counter.incrementRequestCount(currentTime);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

}
