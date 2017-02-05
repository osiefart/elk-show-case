package hello;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by osiefart on 04.02.17.
 */
public class PerformanceFilter extends OncePerRequestFilter {

    @Autowired
    private MetricRegistry metricRegistry;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        final Timer timer = metricRegistry.timer(httpServletRequest.getRequestURI());
        final Timer.Context context = timer.time();

        try {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } finally {
            context.stop();
        }

    }

}
