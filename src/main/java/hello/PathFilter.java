package hello;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by osiefart on 04.02.17.
 */
public class PathFilter extends OncePerRequestFilter {

    public static final String PATH = "PATH";

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        try {
            MDC.put(PATH, String.valueOf(httpServletRequest.getRequestURI()));
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            MDC.remove(PATH);
        }

    }

}
