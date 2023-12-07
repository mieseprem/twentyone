package de.kaasy.twentyone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TwentyOneApplication {
    public static final long COMMON_WEB_TIMEOUT_IN_SECONDS = 10;

    public static void main(String[] args) {
        var applicationContext = SpringApplication.run(TwentyOneApplication.class, args);

        var twentyOneRunner = applicationContext.getBean(TwentyOneRunner.class);
        twentyOneRunner.doYourWork();
    }
}
