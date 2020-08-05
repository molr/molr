package io.molr.mole.core.runnable.demo.conf;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.Branch;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableSet;

import static io.molr.commons.domain.Placeholder.*;

import java.util.Set;

@Configuration
public class DemoRunnableLeafsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoRunnableLeafsConfiguration.class);

    @Bean
    public RunnableLeafsMission demoMission() {
        return new RunnableLeafsMissionSupport() {
            {
                sequential("Executable Leafs Demo Mission", root -> {

                    root.sequential("First", b -> {
                        Block firstABlock = b.run(log("First A"));
                        breakOn(firstABlock);
                        b.run(log("First B"));
                    });

                    root.sequential("Second", b -> {
                        b.run(log("second A"));
                        b.run(log("second B"));
                    });

                    root.run(log("Third"));

                    root.parallel("Parallel", b -> {
                        b.run(log("Parallel A"));
                        b.run(log("parallel B"));
                    });

                });

            }
        }.build();
    }


    @Bean
    public RunnableLeafsMission parametrizedDemoMission() {
        return new RunnableLeafsMissionSupport() {
            {
                Placeholder<Integer> iterations = mandatory(anInteger("iterations"), 5);
                Set<Integer> allowedSleepTimes = ImmutableSet.of(0, 100,200,300, 400, 500);
                /**
                 * optionally add a collection of allowed values in order to restrict parameter values to those values
                 * if allowed parameters is used explicitly, but empty the parameter values are considered to be unrestricted
                 */
                Placeholder<Integer> sleepMilis = mandatory(anInteger("sleepMillis"), 500, allowedSleepTimes);
                Set<String> allowedMessages = ImmutableSet.of("Hello World", "Hello Molr");
                Placeholder<String> message = mandatory(aString("aMessage"), "Hello World", allowedMessages);

                Placeholder<String> device = optional(aString("deviceName"), ImmutableSet.of("TEST_DEVCIE_1", "TEST_DEVICE_2"));
                Placeholder<Double> betax = optional(aDouble("betax"), 180.5);

                sequential("Executable Leafs Demo Mission (parametrized)", root -> {

                    root.run("print messages", (in, out) -> {
                        for (int i = 0; i < in.get(iterations); i++) {
                            LOGGER.info("Iteration=" + i + "; " + in.get(message) + i);
                            sleepUnchecked(in.get(sleepMilis));
                            out.emit("iteration-" + i, in.get(message) + i);
                        }
                    });

                    root.run("print optionals", (in, out) -> {
                        LOGGER.info("device=" + in.get(device));
                        out.emit("device", in.get(device)); /* Will not be added as null is not allowed */

                        LOGGER.info("betax=" + in.get(betax));
                        out.emit("betax", in.get(betax));
                    });

                    root.sequential("First", b -> {
                        b.run(log("First A"));
                        b.run("Failing subtask ", () -> {
                            throw new RuntimeException("Failing on purpose.");
                        });
                        b.run(log("First B"));
                    });

                    root.sequential("Second", b -> {
                        b.run(log("second A"));
                        b.run(log("second B"));
                    });

                    root.run(log("Third"));

                    root.parallel("Parallel", b -> {
                        b.run(log("Parallel A"));
                        b.run(log("parallel B"));
                    });

                });

            }
        }.build();
    }

    @Bean
    public RunnableLeafsMission parallelBlocksMission() {
        return new RunnableLeafsMissionSupport() {
            {
                sequential("Parallel Blocks", root -> {

                    root.parallel("Parallel 1", b -> {
                        b.run(log("Parallel 1A"));
                        b.run(log("parallel 1B"));
                    });

                    root.parallel("Parallel 2", b -> {
                        b.run(log("Parallel 2A"));
                        b.run(log("parallel 2B"));
                    });

                });

            }
        }.build();
    }


    private static Branch.Task log(String text) {
        return new Branch.Task(text, (in, out) -> LOGGER.info("{} executed", text));
    }

    private static void sleepUnchecked(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
