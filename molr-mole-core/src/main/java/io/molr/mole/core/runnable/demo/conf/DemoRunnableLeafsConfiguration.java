package io.molr.mole.core.runnable.demo.conf;

import static io.molr.commons.domain.BlockAttribute.BREAK;
import static io.molr.commons.domain.BlockAttribute.IGNORE;
import static io.molr.commons.domain.Placeholder.aDouble;
import static io.molr.commons.domain.Placeholder.aLong;
import static io.molr.commons.domain.Placeholder.aString;
import static io.molr.commons.domain.Placeholder.anInteger;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.BlockAttribute;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.ListOfStrings;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.runnable.lang.SimpleBranch;

@Configuration
public class DemoRunnableLeafsConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoRunnableLeafsConfiguration.class);

    @Bean
    public static RunnableLeafsMission foreachFromContext() {
        return new RunnableLeafsMissionSupport() {
            {
                root("ForeachWithListFromContext").contextual(in -> {
                    return ImmutableList.of("A", "B");
                }).as((rootBranch, context) -> {
                    rootBranch.foreach(context).branch("device:{}", Placeholders.LATEST_FOREACH_ITEM_PLACEHOLDER)
                            .as((foreachBranch, itemPlaceholder) -> {
                                foreachBranch.leaf("OperateOn {}", itemPlaceholder).runCtx(itemValue -> {
                                    System.out.println(itemValue);
                                });
                            });
                });
            }
        }.build();
    }

    @Bean
    public static RunnableLeafsMission parallelRoot() {
        return new RunnableLeafsMissionSupport() {
            {
                root("ParallelRoot").parallel(2).as(rootBranch -> {
                    rootBranch.leaf("FirstChild").run(() -> {
                        System.out.println("run");
                    });
                    rootBranch.leaf("SecondChild").run(() -> {
                        System.out.println("run " + this);
                    });
                });

            }
        }.build();
    }

    @Bean
    public static RunnableLeafsMission sequentialRoot() {
        return new RunnableLeafsMissionSupport() {
            {
                root("SequentialRoot").sequential().as(rootBranch -> {
                    rootBranch.leaf("A").run(() -> {
                        System.out.println("run");
                    });
                    rootBranch.branch("B").sequential().as(branch -> {
                        branch.leaf("B.A").run(() -> {
                            System.out.println("run nested");
                        });
                        branch.leaf("B.B").run(() -> {
                            System.out.println("run nested");
                        });
                    });
                });

            }
        }.build();
    }

    @Bean
    public static RunnableLeafsMission runStateMission() {
        return new RunnableLeafsMissionSupport() {
            {
                executionStrategy().defaultsTo(ExecutionStrategy.ABORT_ON_ERROR).allowed(ExecutionStrategy.values());

                root("RunStates").sequential().as(root -> {

                    root.branch("Zero").sequential().as(b1 -> {
                        b1.branch("YeroA_First").as(subB1 -> {
                            subB1.leaf("ZAA").run(() -> {/* nothing */});
                            subB1.leaf("ZAB").run(() -> {/* nothing */});
                        });

                        b1.leaf("ZFirst A").run(() -> LOGGER.info("{} executed", "First A"));
                        b1.leaf("ZFirstB").run(() -> {/* nothing */});
                    });

                    root.branch("First").parallel().as(b1 -> {
                        b1.branch("FirstA_First").as(subB1 -> {
                            subB1.leaf("AA").run(() -> {/* nothing */});
                            subB1.leaf("AB").run(() -> {
                                throw new IllegalStateException("e");
                            });

                        });

                        b1.leaf("First A").run(() -> LOGGER.info("{} executed", "First A"));
                        b1.leaf("FirstB").run(() -> {/* nothing */});
                    });
                });

            }
        }.build();
    }

    @Bean
    public static RunnableLeafsMission runStateMission2() {
        return new RunnableLeafsMissionSupport() {
            {
                executionStrategy().defaultsTo(ExecutionStrategy.ABORT_ON_ERROR).allowed(ExecutionStrategy.values());

                root("RunStates2").sequential().as(root -> {

                    root.branch("ParallelParallel").sequential().as(b1 -> {
                        b1.branch("YeroA_First").sequential().as(subB1 -> {
                            subB1.leaf("ZAA").run(() -> {/* nothing */});
                            subB1.leaf("ZAB").run(() -> {
                                throw new RuntimeException("error");
                            });
                        });

                        b1.leaf("ZFirst A").run(() -> LOGGER.info("{} executed", "First A"));
                        b1.leaf("ZFirstB").run(() -> {/* nothing */});
                    });

                });

            }
        }.build();
    }

    @Bean
    public static RunnableLeafsMission demoMission() {
        return new RunnableLeafsMissionSupport() {
            {
                executionStrategy().defaultsTo(ExecutionStrategy.PAUSE_ON_ERROR).allowed(ExecutionStrategy.values());

                root("Executable Leafs Demo Mission").sequential().as(root -> {

                    root.branch("First").sequential().as(b1 -> {
                        b1.leaf("First A")
                                //.perDefault(BREAK)
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
    public static RunnableLeafsMission parametrizedDemoMission() {
        return new RunnableLeafsMissionSupport() {
            {
                Placeholder<Integer> iterations = mandatory(anInteger("iterations"), 5);
                Set<Integer> allowedSleepTimes = ImmutableSet.of(0, 100, 200, 300, 400, 500);
                /**
                 * optionally add a collection of allowed values in order to restrict parameter values to those values
                 * if allowed parameters is used explicitly, but empty the parameter values are considered to be
                 * unrestricted
                 */
                Placeholder<Integer> sleepMilis = mandatory(anInteger("sleepMillis"), 500, allowedSleepTimes);
                Placeholder<Long> sleepAsLongPlaceholder = mandatory(aLong("sleepMillis2"), 999L);
                Set<String> allowedMessages = ImmutableSet.of("Hello World", "Hello Molr");
                Placeholder<String> message = mandatory(aString("aMessage"), "Hello World", allowedMessages);

                ListOfStrings allowedItems = new ListOfStrings("A", "B", "C", "D", "E", "F");
                /*
                 * For the moment the collection containing allowed items have to be wrapped into a set since allowed
                 * values has originally been created for non-collection types
                 * This issue will be addressed in upcoming releases
                 */
                Set<ListOfStrings> wrappedAllowedValues = new HashSet<>();
                wrappedAllowedValues.add(allowedItems);
                ListOfStrings defaultItems = new ListOfStrings("B");
                Placeholder<ListOfStrings> devs = mandatory(Placeholder.aListOfStrings("devices"), defaultItems,
                        wrappedAllowedValues);

                Placeholder<String> device = optional(aString("deviceName"), "TEST_DEVCIE_1",
                        ImmutableSet.of("TEST_DEVCIE_1", "TEST_DEVICE_2"));
                Placeholder<Double> betax = optional(aDouble("betax"), 180.5);

                root("Executable Leafs Demo Mission (parametrized)").sequential().as(root -> {

                    root.leaf("print messages").run((in1, out1) -> {
                        ListOfStrings deviceNames = in1.get(devs);
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

                    root.branch("Second {}", devs).sequential().as(b1 -> {
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
    public static RunnableLeafsMission parallelBlocksMission() {

        return new RunnableLeafsMissionSupport() {
            {
                root("Parallel Blocks").sequential().as(root -> {

                    root.branch("Parallel 1").parallel().as(b1 -> {
                        log(b1, "Parallel 1A");
                        log(b1, "parallel 1B");
                    });

                    root.branch("Parallel 2").parallel().as(b -> {
                        log(b, "Parallel 2A");
                        log(b, "parallel 2B");

                    });
                });

            }
        }.build();
    }

    @Bean
    public static RunnableLeafsMission foreachMission() {
        RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
            {
                ListOfStrings allowedItems = new ListOfStrings("A", "B", "C", "D", "E", "F");
                for (int i = 0; i < 250; i++) {
                    allowedItems.add("A" + i);
                }
                /*
                 * For the moment the collection containing allowed items have to be wrapped into a set since allowed
                 * values has originally been created for non-collection types
                 * This issue will be addressed in upcoming releases
                 */
                Set<ListOfStrings> wrappedAllowedValues = new HashSet<>();
                wrappedAllowedValues.add(allowedItems);
                ListOfStrings defaultItems = new ListOfStrings("B");
                Placeholder<ListOfStrings> someDevices = mandatory(Placeholder.aListOfStrings("deviceNames"),
                        defaultItems, wrappedAllowedValues);
                /*
                 * map called on foreach will map/transform each item into an object created by the given factory
                 */
                root("foreachDemo").foreach(someDevices).parallel().map(DeviceDriver::new)
                        .branch("work on device {} branch", Placeholders.LATEST_FOREACH_ITEM_PLACEHOLDER)
                        .as((doWithDeviceBranch, devicePlaceholder) -> {
                            doWithDeviceBranch.leaf("SwitchOn ").perDefault(IGNORE).runCtx(device -> {
                                device.switchOn();
                            });
                            doWithDeviceBranch.leaf("Pause").perDefault(IGNORE).run(() -> Thread.sleep(1000));
                            doWithDeviceBranch.leaf("SwitchOff {}", devicePlaceholder).runCtx(device -> {
                                device.switchOff();
                            });
                        });
            }
        }.build();
        return mission;
    }

    @Bean
    public static RunnableLeafsMission foreachMissionWithoutMappedItem() {
        RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
            {
                ListOfStrings allowedItems = new ListOfStrings("A", "B", "C", "D", "E", "F");
                /*
                 * For the moment the collection containing allowed items have to be wrapped into a set since allowed
                 * values has originally been created for non-collection types
                 * This issue will be addressed in upcoming releases
                 */
                Set<ListOfStrings> wrappedAllowedValues = new HashSet<>();
                wrappedAllowedValues.add(allowedItems);
                ListOfStrings defaultItems = new ListOfStrings("B", "C", "F");
                Placeholder<ListOfStrings> someDevices = mandatory(Placeholder.aListOfStrings("deviceNames"),
                        defaultItems, wrappedAllowedValues);

                root("foreachDemoWithoutMappedItem").foreach(someDevices).branch("workOnDeviceBranch")
                        .as((doWithDeviceBranch, devicePlaceholder) -> {
                            doWithDeviceBranch.leaf("SwitchOn ").runCtx(device -> {/* nothing */});
                            doWithDeviceBranch.leaf("Pause").run(() -> Thread.sleep(1000));
                            doWithDeviceBranch.leaf("SwitchOff ").runCtx(device -> {/* nothing */});
                        });
            }
        }.build();
        return mission;
    }

    @Bean
    public static RunnableLeafsMission foreachMissionThrowingException() {
        RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
            {
                ListOfStrings allowedItems = new ListOfStrings("A", "B", "C", "D", "E", "F");
                /*
                 * For the moment the collection containing allowed items have to be wrapped into a set since allowed
                 * values has originally been created for non-collection types
                 * This issue will be addressed in upcoming releases
                 */
                Set<ListOfStrings> wrappedAllowedValues = new HashSet<>();
                wrappedAllowedValues.add(allowedItems);
                ListOfStrings defaultItems = new ListOfStrings("B");
                Placeholder<ListOfStrings> someDevices = mandatory(Placeholder.aListOfStrings("deviceNames"),
                        defaultItems, wrappedAllowedValues);

                /*
                 * optionally configure allowed and default execution strategies.
                 */
                executionStrategy().defaultsTo(ExecutionStrategy.PROCEED_ON_ERROR).allowAll();

                root("foreachDemoWithSelectableExecutionStrategy").foreach(someDevices).parallel(2)
                        .map(DeviceDriver::new).branch("workOnDeviceBranch")
                        .as((doWithDeviceBranch, devicePlaceholder) -> {
                            doWithDeviceBranch.leaf("SwitchOn ").runCtx(device -> {
                                device.switchOn();
                            });
                            doWithDeviceBranch.leaf("Pause").run(() -> Thread.sleep(1000));
                            doWithDeviceBranch.leaf("ThrowException")
                                    .perDefault(BlockAttribute.ON_ERROR_SKIP_SEQUENTIAL_SIBLINGS).run(() -> {
                                        throw new RuntimeException("error xy");
                                    });
                            doWithDeviceBranch.leaf("SwitchOff ").runCtx(device -> {
                                device.switchOff();
                            });
                        });
            }
        }.build();
        return mission;
    }

    @Bean
    public static RunnableLeafsMission contextualRunnableLeafsMission() {
        return new RunnableLeafsMissionSupport() {
            {
                Placeholder<String> device = mandatory(aString("deviceName"));
                Placeholder<String> a = mandatory(aString("a"));
                Placeholder<String> b = mandatory(aString("b"));

                root("contextual mission").sequential().perDefaultDont(BREAK).contextual(DeviceDriver::new, device)
                        .as((root, ctx) -> {
                            root.leaf("switch on").perDefault(BREAK).runCtx(DeviceDriver::switchOn);
                            root.leaf("switch off {}", device).runCtx(DeviceDriver::switchOff);
                            root.leaf("do").runCtx((DeviceDriver dev, String A, String B) -> {

                                System.out.println("run");

                            }, a, b);
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

        @Override
        public String toString() {
            return MessageFormat.format("'{'deviceName={0}'}'", deviceName);
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
