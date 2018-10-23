package org.molr.mole.core.runnable.demo.conf;

import org.molr.mole.core.runnable.RunnableLeafsMission;
import org.molr.mole.core.runnable.lang.RunnableBranchSupport;
import org.molr.mole.core.runnable.lang.RunnableMissionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoRunnableLeafsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoRunnableLeafsConfiguration.class);


    @Bean
    public RunnableLeafsMission demoMission() {
        return new RunnableMissionSupport() {
            {
                mission("Root", root -> {

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



    private static RunnableBranchSupport.Task log(String text) {
        return new RunnableBranchSupport.Task(text, () -> LOGGER.info("{} executed", text));
    }
}
