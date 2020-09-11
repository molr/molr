package io.molr.mole.core.runnable.demo.conf;

import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.ListOfStrings;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.SimpleBranch;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableSet;

import static io.molr.commons.domain.Placeholder.*;
import static io.molr.mole.core.runnable.lang.BlockAttribute.BREAK;

import java.util.Set;

@Configuration
public class DemoRunnableLeafsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoRunnableLeafsConfiguration.class);

    @Bean
    public RunnableLeafsMission demoMission() {
        return new RunnableLeafsMissionSupport() {
            {
                root("Executable Leafs Demo Mission").sequential().as(root -> {

                    root.branch("First").sequential().as(b1 -> {
                        b1.leaf("First A")
                                .perDefault(BREAK)
                                .run(() -> LOGGER.info("{} executed", "First A"));
                        log(b1, "First B");
                    });

                    root.branch("Second").sequential().as(b1 -> {
                        log(b1, "second A");
                        log(b1, "second B");
                    });

                    log(root, "Third");

                    root.branch("Parallel").parallel().as(b -> {
                        log(b, "Parallel A");
                        log(b, "parallel B");
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
                //TODO validate parameter description - duplicate keys?!
                Placeholder<Integer> sleepMilis = mandatory(anInteger("sleepMillis"), 500, allowedSleepTimes);
                Placeholder<Long> sleepAsLongPlaceholder = mandatory(aLong("sleepMillis2"), 999L);
                Set<String> allowedMessages = ImmutableSet.of("Hello World", "Hello Molr");
                Placeholder<String> message = mandatory(aString("aMessage"), "Hello World", allowedMessages);

                Placeholder<ListOfStrings> devices = mandatory(Placeholder.aListOfStrings("devices"));
                
                Placeholder<String> device = optional(aString("deviceName"), "TEST_DEVCIE_1", ImmutableSet.of("TEST_DEVCIE_1", "TEST_DEVICE_2"));
                Placeholder<Double> betax = optional(aDouble("betax"), 180.5);

                root("Executable Leafs Demo Mission (parametrized)").sequential().as(root -> {
                    
                    root.leaf("print messages").run((in1, out1) -> {
                        ListOfStrings deviceNames = in1.get(devices);
                        Long sleepAsLong = in1.get(sleepAsLongPlaceholder);
                        LOGGER.info(sleepAsLong.toString());
                        LOGGER.info(deviceNames.toString());
                        for (int i = 0; i < in1.get(iterations); i++) {
                            LOGGER.info("Iteration=" + i + "; " + in1.get(message) + i);
                            sleepUnchecked(in1.get(sleepMilis));
                            out1.emit("iteration-" + i, in1.get(message) + i);
                        }
                    });

                    root.leaf("print optionals").run((in, out) -> {
                        LOGGER.info("device=" + in.get(device));
                        out.emit("device", in.get(device)); /* Will not be added as null is not allowed */

                        LOGGER.info("betax=" + in.get(betax));
                        out.emit("betax", in.get(betax));
                    });

                    root.branch("First").sequential().as(b1 -> {
                        log(b1, "First A");
                        b1.leaf("Failing subtask ").run(() -> {
                            throw new RuntimeException("Failing on purpose.");
                        });
                        log(b1, "First B");
                    });

                    root.branch("Second").sequential().as(b1 -> {
                        log(b1, "second A");
                        log(b1, "second B");
                    });

                    log(root, "Third");

                    root.branch("Parallel").parallel().as(b -> {
                        log(b, "Parallel A");
                        log(b, "parallel B");
                    });

                });

            }
        }.build();
    }

    @Bean
    public RunnableLeafsMission parallelBlocksMission() {
        return new RunnableLeafsMissionSupport() {
            {
                root("Parallel Blocks").sequential().as(root -> {

                    root.branch("Parallel 1").parallel().as( b1 -> {
                        log(b1, "Parallel 1A");
                        log(b1, "parallel 1B");
                    });

                    root.branch("Parallel 2").parallel().as( b -> {
                        log(b, "Parallel 2A");
                        log(b, "parallel 2B");
                    });

                });

            }
        }.build();
    }

    @Bean
    public RunnableLeafsMission contextualRunnableLeafsMission() {
        return new RunnableLeafsMissionSupport() {
            {
                Placeholder<String> device = mandatory(aString("deviceName"));

                root("contextual mission").sequential().perDefaultDont(BREAK).contextual(DeviceDriver::new, device).as(root -> {
                    root.leaf("switch on").perDefault(BREAK).runCtx(DeviceDriver::switchOn);
                    root.leaf("switch off").runCtx(DeviceDriver::switchOff);
                });

            }
        }.build();
    }
    
    @Bean
    RunnableLeafsMission forEachExperiment() {

        return new RunnableLeafsMissionSupport() {
            {

                Placeholder<ListOfStrings> collectionPlaceholder = mandatory(Placeholder.aListOfStrings("devices"));
                Placeholder<String> itemPlaceholder = Placeholder.aString("localDevice");
                optional(Placeholders.EXECUTION_STRATEGY, ExecutionStrategy.ABORT_ON_ERROR.name());
                
                root("forEachMissionExperiment").sequential().as(missionRoot -> {// 0
                    missionRoot.leafForEach("aForEachLoop", collectionPlaceholder, itemPlaceholder, (in, out) -> {
                        String deviceName = in.get(itemPlaceholder);
                        System.out.println("deviceName: " + deviceName);
                    });
                    missionRoot.branch("main1").parallel().as(main1Root -> {// 1

                        main1Root.leaf("main1Sub1").run(() -> {// 2
                            System.out.println("main1Sub1");
                        });

                        main1Root.branch("main1Sub2").sequential().as(main1Sub2 -> {// 3

                            main1Sub2.leaf("main1Sub2Sub1").run(() -> {// 4
                                System.out.println("main1Sub2Task1");
                            });

                            main1Sub2.leaf("main1Sub2Sub2").run(() -> {// 5
                                System.out.println("main1Sub2Task2");
                            });

                            main1Sub2.leaf("main1Sub2Sub3").run(() -> {// 6
                                System.out.println("main1Sub2Task3");
                            });
                        });

                        main1Root.leaf("main1Sub3").run(() -> {// 7
                            System.out.println("main1Sub1");
                        });
                    });

                    missionRoot.leaf("main2").run((in, out) -> {// 8
                        System.out.println("hello1");
                    });
                    missionRoot.leaf("main3").run((in, out) -> {// 9
                        System.out.println("hello2");
                    });
                });
            }
        }.build();

    }

    private static class DeviceDriver {

        private final String deviceName;

        public DeviceDriver(String deviceName) {
            this.deviceName = deviceName;
        }

        public void switchOn() {
            System.out.println("Switched ON device " + deviceName + ".");
        }

        public void switchOff() {
            System.out.println("Switched OFF device " + deviceName + ".");
        }


    }

    private static void log(SimpleBranch b, String text) {
        b.leaf(text).run(() -> LOGGER.info("{} executed", text));
    }

    private static void sleepUnchecked(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
