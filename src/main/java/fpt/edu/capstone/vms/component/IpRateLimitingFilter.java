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
    private final int maxCalls = 30;
    private final long timeWindowInMillis = 30000; // 10 minutes
    private final String UPLOAD = "/api/v1/file/uploadImage";
    private final String CARD = "/api/v1/card";
    private final String CARD_SCAN = "/api/v1/card/scan";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String requestUri = httpRequest.getRequestURI();

        if (UPLOAD.equals(requestUri) || CARD.equals(requestUri) || CARD_SCAN.equals(requestUri)) {
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
                    servletResponse.getWriter().write("Too many requests from this IP address. Please try again later.");
                    return;
                }

                counter.incrementRequestCount(currentTime);
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

}
