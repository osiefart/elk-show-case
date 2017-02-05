package hello;

import com.codahale.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

/**
 * A reporter class for logging metrics values to a SLF4J {@link Logger} periodically, similar to
 * {@link ConsoleReporter} or {@link CsvReporter}, but using the SLF4J framework instead. It also
 * supports specifying a {@link Marker} instance that can be used by custom appenders and filters
 * for the bound logging toolkit to further process metrics reports.
 */
public class LogstashReporter extends ScheduledReporter {
    /**
     * Returns a new {@link hello.LogstashReporter.Builder} for {@link hello.LogstashReporter}.
     *
     * @param registry the registry to report
     * @return a {@link hello.LogstashReporter.Builder} instance for a {@link hello.LogstashReporter}
     */
    public static hello.LogstashReporter.Builder forRegistry(MetricRegistry registry) {
        return new hello.LogstashReporter.Builder(registry);
    }

    public enum LoggingLevel {TRACE, DEBUG, INFO, WARN, ERROR}

    /**
     * A builder for {@link hello.LogstashReporter} instances. Defaults to logging to {@code metrics}, not
     * using a marker, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private Logger logger;
        private hello.LogstashReporter.LoggingLevel loggingLevel;
        private Marker marker;
        private String prefix;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.logger = LoggerFactory.getLogger("metrics");
            this.marker = null;
            this.prefix = "";
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.loggingLevel = hello.LogstashReporter.LoggingLevel.INFO;
        }

        /**
         * Log metrics to the given logger.
         *
         * @param logger an SLF4J {@link Logger}
         * @return {@code this}
         */
        public hello.LogstashReporter.Builder outputTo(Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * Mark all logged metrics with the given marker.
         *
         * @param marker an SLF4J {@link Marker}
         * @return {@code this}
         */
        public hello.LogstashReporter.Builder markWith(Marker marker) {
            this.marker = marker;
            return this;
        }

        /**
         * Prefix all metric names with the given string.
         *
         * @param prefix the prefix for all metric names
         * @return {@code this}
         */
        public hello.LogstashReporter.Builder prefixedWith(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public hello.LogstashReporter.Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public hello.LogstashReporter.Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public hello.LogstashReporter.Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Use Logging Level when reporting.
         *
         * @param loggingLevel a (@link LogstashReporter.LoggingLevel}
         * @return {@code this}
         */
        public hello.LogstashReporter.Builder withLoggingLevel(hello.LogstashReporter.LoggingLevel loggingLevel) {
            this.loggingLevel = loggingLevel;
            return this;
        }

        /**
         * Builds a {@link hello.LogstashReporter} with the given properties.
         *
         * @return a {@link hello.LogstashReporter}
         */
        public hello.LogstashReporter build() {
            hello.LogstashReporter.LoggerProxy loggerProxy;
            switch (loggingLevel) {
                case TRACE:
                    loggerProxy = new hello.LogstashReporter.TraceLoggerProxy(logger);
                    break;
                case INFO:
                    loggerProxy = new hello.LogstashReporter.InfoLoggerProxy(logger);
                    break;
                case WARN:
                    loggerProxy = new hello.LogstashReporter.WarnLoggerProxy(logger);
                    break;
                case ERROR:
                    loggerProxy = new hello.LogstashReporter.ErrorLoggerProxy(logger);
                    break;
                default:
                case DEBUG:
                    loggerProxy = new hello.LogstashReporter.DebugLoggerProxy(logger);
                    break;
            }
            return new hello.LogstashReporter(registry, loggerProxy, marker, prefix, rateUnit, durationUnit, filter);
        }
    }

    private final hello.LogstashReporter.LoggerProxy loggerProxy;
    private final Marker marker;
    private final String prefix;

    private LogstashReporter(MetricRegistry registry,
                             hello.LogstashReporter.LoggerProxy loggerProxy,
                             Marker marker,
                             String prefix,
                             TimeUnit rateUnit,
                             TimeUnit durationUnit,
                             MetricFilter filter) {
        super(registry, "logger-reporter", filter, rateUnit, durationUnit);
        this.loggerProxy = loggerProxy;
        this.marker = marker;
        this.prefix = prefix;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        if (loggerProxy.isEnabled(marker)) {
            for (Entry<String, Gauge> entry : gauges.entrySet()) {
                logGauge(entry.getKey(), entry.getValue());
            }

            for (Entry<String, Counter> entry : counters.entrySet()) {
                logCounter(entry.getKey(), entry.getValue());
            }

            for (Entry<String, Histogram> entry : histograms.entrySet()) {
                logHistogram(entry.getKey(), entry.getValue());
            }

            for (Entry<String, Meter> entry : meters.entrySet()) {
                logMeter(entry.getKey(), entry.getValue());
            }

            for (Entry<String, Timer> entry : timers.entrySet()) {
                logTimer(entry.getKey(), entry.getValue());
            }
        }
    }

    private void logTimer(String name, Timer timer) {
        final Snapshot snapshot = timer.getSnapshot();
        loggerProxy.log(marker,
                "TIMER: {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
                keyValue("type", "TIMER"),
                keyValue("name", prefix(name)),
                keyValue("count", timer.getCount()),
                keyValue("min", convertDuration(snapshot.getMin())),
                keyValue("max", convertDuration(snapshot.getMax())),
                keyValue("mean", convertDuration(snapshot.getMean())),
                keyValue("stddev", convertDuration(snapshot.getStdDev())),
                keyValue("median", convertDuration(snapshot.getMedian())),
                keyValue("p75", convertDuration(snapshot.get75thPercentile())),
                keyValue("p95", convertDuration(snapshot.get95thPercentile())),
                keyValue("p98", convertDuration(snapshot.get98thPercentile())),
                keyValue("p99", convertDuration(snapshot.get99thPercentile())),
                keyValue("p999", convertDuration(snapshot.get999thPercentile())),
                keyValue("mean_rate", convertRate(timer.getMeanRate())),
                keyValue("m1", convertRate(timer.getOneMinuteRate())),
                keyValue("m5", convertRate(timer.getFiveMinuteRate())),
                keyValue("m15", convertRate(timer.getFifteenMinuteRate())),
                keyValue("rate_unit", getRateUnit()),
                keyValue("duration_unit", getDurationUnit()));
    }

    private void logMeter(String name, Meter meter) {
        loggerProxy.log(marker,
                "METER: {}, {}, {}, {}, {}, {}, {}, {}",
                keyValue("type", "METER"),
                keyValue("name", prefix(name)),
                keyValue("count", meter.getCount()),
                keyValue("mean_rate", convertRate(meter.getMeanRate())),
                keyValue("m1", convertRate(meter.getOneMinuteRate())),
                keyValue("m5", convertRate(meter.getFiveMinuteRate())),
                keyValue("m15", convertRate(meter.getFifteenMinuteRate())),
                keyValue("rate_unit", getRateUnit()));
    }

    private void logHistogram(String name, Histogram histogram) {
        final Snapshot snapshot = histogram.getSnapshot();
        loggerProxy.log(marker,
                "Histogram: {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}",
                keyValue("type", "HISTOGRAM"),
                keyValue("name", prefix(name)),
                keyValue("count", histogram.getCount()),
                keyValue("min", snapshot.getMin()),
                keyValue("max", snapshot.getMax()),
                keyValue("mean", snapshot.getMean()),
                keyValue("stddev", snapshot.getStdDev()),
                keyValue("median", snapshot.getMedian()),
                keyValue("p75", snapshot.get75thPercentile()),
                keyValue("p95", snapshot.get95thPercentile()),
                keyValue("p98", snapshot.get98thPercentile()),
                keyValue("p99", snapshot.get99thPercentile()),
                keyValue("p999", snapshot.get999thPercentile()));
    }

    private void logCounter(String name, Counter counter) {
        loggerProxy.log(marker, "COUNTER: {}, {}, {}", keyValue("type", "COUNTER"), keyValue("name", prefix(name)), keyValue("count", counter.getCount()));
    }

    private void logGauge(String name, Gauge gauge) {
        loggerProxy.log(marker, "GAUGE: {}, {}, {}", keyValue("type", "GAUGE"), keyValue("name", prefix(name)), keyValue("value", gauge.getValue()));
    }

    @Override
    protected String getRateUnit() {
        return "events/" + super.getRateUnit();
    }

    private String prefix(String... components) {
        return MetricRegistry.name(prefix, components);
    }

    /* private class to allow logger configuration */
    static abstract class LoggerProxy {
        protected final Logger logger;

        public LoggerProxy(Logger logger) {
            this.logger = logger;
        }

        abstract void log(Marker marker, String format, Object... arguments);

        abstract boolean isEnabled(Marker marker);
    }

    /* private class to allow logger configuration */
    private static class DebugLoggerProxy extends hello.LogstashReporter.LoggerProxy {
        public DebugLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format, Object... arguments) {
            logger.debug(marker, format, arguments);
        }

        @Override
        public boolean isEnabled(Marker marker) {
            return logger.isDebugEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class TraceLoggerProxy extends hello.LogstashReporter.LoggerProxy {
        public TraceLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format, Object... arguments) {
            logger.trace(marker, format, arguments);
        }

        @Override
        public boolean isEnabled(Marker marker) {
            return logger.isTraceEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class InfoLoggerProxy extends hello.LogstashReporter.LoggerProxy {
        public InfoLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format, Object... arguments) {
            logger.info(marker, format, arguments);
        }

        @Override
        public boolean isEnabled(Marker marker) {
            return logger.isInfoEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class WarnLoggerProxy extends hello.LogstashReporter.LoggerProxy {
        public WarnLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format, Object... arguments) {
            logger.warn(marker, format, arguments);
        }

        @Override
        public boolean isEnabled(Marker marker) {
            return logger.isWarnEnabled(marker);
        }
    }

    /* private class to allow logger configuration */
    private static class ErrorLoggerProxy extends hello.LogstashReporter.LoggerProxy {
        public ErrorLoggerProxy(Logger logger) {
            super(logger);
        }

        @Override
        public void log(Marker marker, String format, Object... arguments) {
            logger.error(marker, format, arguments);
        }

        @Override
        public boolean isEnabled(Marker marker) {
            return logger.isErrorEnabled(marker);
        }
    }

}
