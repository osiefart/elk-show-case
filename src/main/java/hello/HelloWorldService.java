package hello;

import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by osiefart on 05.02.17.
 */
@Service
public class HelloWorldService {

    public String hello() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "hi ";
    }

}
