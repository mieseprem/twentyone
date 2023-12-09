package de.kaasy.twentyone;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class TwentyOneApplication {
    public static final long COMMON_WEB_TIMEOUT_IN_SECONDS = 10;
    static final ThreadFactory THREAD_FACTORY =
            Thread.ofVirtual().name("virt-", 0L).factory();

    final TwentyOneService twentyOneService;

    public static void main(final String[] args) {
        var applicationContext = SpringApplication.run(TwentyOneApplication.class, args);

        var twentyOneRunner = applicationContext.getBean(TwentyOneApplication.class);
        twentyOneRunner.doYourWork(20);
    }

    void doYourWork(final int numberOfItemsToProcess) {
        log.info("Started");
        // if you really want to limit the parallelism
        try (var executorService = Executors.newFixedThreadPool(10, TwentyOneApplication.THREAD_FACTORY)) {
            AtomicInteger batchProcess = new AtomicInteger();

            // Simulate workload by generating a series of integers we'll later use as delay value
            ThreadLocalRandom.current()
                    .ints(
                            numberOfItemsToProcess,
                            1,
                            Math.toIntExact(TwentyOneApplication.COMMON_WEB_TIMEOUT_IN_SECONDS) - 1)
                    .forEach(intUsedForEverything -> {
                        var batchNumber = batchProcess.incrementAndGet();

                        executorService.submit(() -> {
                            // vvvvvvvv "business logic" vvvvvvvv
                            // #
                            // # for every workload item data has to be collected
                            var yesNoCatFact = twentyOneService.getData(batchNumber);
                            // #
                            // # afterward data should be written to somewhere
                            twentyOneService.storeData(batchNumber, intUsedForEverything, yesNoCatFact);
                            // #
                            // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                        });
                    });
        }
        log.info("Done");
    }
}
