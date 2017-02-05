package hello;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by osiefart on 04.02.17.
 */
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID = "requestId";

    private static AtomicInteger requestIdCounter = new AtomicInteger();

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        int requestId = requestIdCounter.getAndIncrement();
        try {
            MDC.put(REQUEST_ID, String.valueOf(requestId));
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            MDC.remove(REQUEST_ID);
        }

    }

}
