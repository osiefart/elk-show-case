package hello;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by osiefart on 05.02.17.
 */
@Aspect
@Component
public class ServicePerformanceMonitor {

    @Autowired
    private MetricRegistry metricRegistry;

    @Around("execution(* hello.HelloWorldService.*(..))")
    public Object logServiceAccess(ProceedingJoinPoint joinPoint) throws Throwable {

        final Timer timer = metricRegistry.timer(MDC.get(PathFilter.PATH) + "#" + joinPoint.getSignature().getName());
        final Timer.Context context = timer.time();

        try {
            return joinPoint.proceed();
        } finally {
            context.stop();
        }

    }

}