package hello;

import net.logstash.logback.composite.loggingevent.ArgumentsJsonProvider;
import net.logstash.logback.encoder.LogstashEncoder;

/**
 * Created by osiefart on 05.02.17.
 */
public class LogstashEncoderWithStructuredArguments extends LogstashEncoder {
    public LogstashEncoderWithStructuredArguments() {
        addProvider(new ArgumentsJsonProvider());
    }
}
