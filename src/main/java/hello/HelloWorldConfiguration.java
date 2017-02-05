package hello;

import com.codahale.metrics.MetricRegistry;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.servlet.Filter;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class HelloWorldConfiguration {

    @Autowired
    private MetricRegistry metricRegistry;

    /*
    @Bean
    public ConsoleReporter consoleReporter() {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(5, TimeUnit.SECONDS);
        return reporter;
    }

   */

    @Bean
    public Filter pathFilter() {
        return new PathFilter();
    }

    @Bean
    public Filter performanceFilter() {
        return new PerformanceFilter();
    }

    @Bean
    public Filter requestIdFilter() {
        return new RequestIdFilter();
    }

    @Bean
    public LogstashReporter logstashReporter() {
        LogstashReporter reporter = LogstashReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger("metrics"))
                .withLoggingLevel(LogstashReporter.LoggingLevel.INFO)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(5, TimeUnit.SECONDS);
        return reporter;
    }


    public static void main(String[] args) {
        SpringApplication.run(HelloWorldConfiguration.class, args);
    }

}
