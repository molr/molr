package org.molr.mole.core.runnable.demo.conf;

import org.molr.commons.domain.Placeholder;
import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.Branch;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.molr.commons.domain.Placeholder.aDouble;
import static org.molr.commons.domain.Placeholder.aString;
import static org.molr.commons.domain.Placeholder.anInteger;

@Configuration
public class DemoRunnableLeafsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoRunnableLeafsConfiguration.class);


    @Bean
    public RunnableLeafsMission demoMission() {
        return new RunnableMissionSupport() {
            {
                mission("Executable Leafs Demo Mission", root -> {

                    root.sequential("First", b -> {
                        b.run(log("First A"));
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
        return new RunnableMissionSupport() {
            {
                Placeholder<Integer> iterations = mandatory(anInteger("iterations"), 5);
                Placeholder<String> message = mandatory(aString("aMessage"), "Hello World");

                Placeholder<String> device = optional(aString("deviceName"));
                Placeholder<Double> betax = optional(aDouble("betax"), 180.5);

                mission("Executable Leafs Demo Mission (parametrized)", root -> {

                    root.run("print messages", (in, out) -> {
                        for (int i = 0; i < in.get(iterations); i++) {
                            LOGGER.info("Iteration=" + i + "; " + in.get(message) + i);
                            out.emit("iteration-" + i, in.get(message) + i);
                        }
                    });

                    root.run("print optionals", in -> {
                        LOGGER.info("device=" + in.get(device));
                        LOGGER.info("betax=" + in.get(betax));
                    });

                    root.sequential("First", b -> {
                        b.run(log("First A"));
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
        return new RunnableMissionSupport() {
            {
                mission("Parallel Blocks", root -> {

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
}
