package io.molr.mole.core.tree;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.Sets;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.ListOfStrings;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.RunnableLeafsMole;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;

/**
 * @author krepp
 */
@SuppressWarnings("static-method")
public class RunnableLeafsMoIeForeachIntegrationTest {

    private final static List<String> ITEM_LIST = new ArrayList<>(Arrays.asList("A", "B", "C"));
    private final static List<String> ITEM_LIST_2 = new ArrayList<>(Arrays.asList("D", "E", "F"));
    private final static String PARAMETER_NAME_DEVICE_NAMES = "deviceNames";
    private final static String PARAMETER_NAME_DEVICE_NAMES_2 = "deviceNames2";

    static RunnableLeafsMission mission() {

        return new RunnableLeafsMissionSupport() {
            {

                Placeholder<ListOfStrings> someDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES));
                Placeholder<ListOfStrings> moreDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES_2));

                executionStrategy().defaultsTo(ExecutionStrategy.ABORT_ON_ERROR);

                root("foreachDemo").sequential().as(missionRoot -> {

                    missionRoot.foreach(someDevices).parallel().leaf("switchOn").runCtx((String item) -> {
                        System.out.println("switchOn: " + item);
                    });

                    missionRoot.foreach(someDevices).parallel().branch("configure").sequential()
                            .as((branchDescription, itemPlaceholder) -> {
                                branchDescription.leaf("setValue").runCtx((item) -> {

                                    System.out.println("setValue of " + item + " to xy");
                                    try {
                                        Thread.sleep(2000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    System.out.println("setValue finished");

                                });

                                branchDescription.foreach(moreDevices).leaf("doSomethingInNestedLoop")
                                        .runCtx((item, in, out) -> {
                                            String outerItem = in.get(itemPlaceholder);
                                            System.out.println(
                                                    "nestedTask " + item + " in outerForeach for item " + outerItem);
                                            out.emit("nestedItems", outerItem + ":" + item);
                                        });

                                branchDescription.leaf("setAnotherValue").runCtx((String item) -> {
                                    System.out.println("setValue of " + item + " to xy");
                                    Thread.sleep(1000);
                                });

                                branchDescription.leaf("switchOff").runCtx((String item) -> {
                                    System.out.println("switchOff " + item);
                                    Thread.sleep(1000);
                                });
                            });
                });
            }

        }.build();

    }

    @Test
    public void nestedForeachAndContexts() throws InterruptedException {
        RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
            {
                Placeholder<ListOfStrings> someDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES));
                Placeholder<ListOfStrings> moreDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES_2));

                root("foreachDemo").foreach(someDevices).branch("workOnDeviceBranch")
                        .as((doWithDeviceBranch, device) -> {
                            doWithDeviceBranch.branch("context").contextual(DemoContext::new, device)
                                    .as((doWithDeviceContextBranch, deviceContext) -> {
                                        doWithDeviceContextBranch.leaf("doSommethingWithDevice")
                                                .runCtx(demoContext -> System.out
                                                        .println("Let's work with nested devices of " + demoContext));
                                        doWithDeviceContextBranch.foreach(moreDevices).map(DemoContext::new)
                                                .branch("nestedDevices").as((nestedDevicesBranch, nestedDevice) -> {
                                                    nestedDevicesBranch.branch("nestedContext")
                                                            .as((nestedContextBranch) -> {
                                                                nestedContextBranch.leaf("doSomeThingInNestedContext")
                                                                        .runCtx(RunnableLeafsMoIeForeachIntegrationTest::doWithDevices,
                                                                                deviceContext);
                                                            });
                                                });
                                    });
                        });
            }
        }.build();
        Mole mole = new RunnableLeafsMole(Sets.newHashSet(mission));
        Map<String, Object> params = new HashMap<>();
        params.put(PARAMETER_NAME_DEVICE_NAMES, ITEM_LIST);
        params.put(PARAMETER_NAME_DEVICE_NAMES_2, ITEM_LIST_2);
        MissionHandle handle = mole.instantiate(new Mission("foreachDemo"), params).block();
        Thread.sleep(50);
        mole.instructRoot(handle, StrandCommand.RESUME);
        mole.statesFor(handle).blockLast();
    }

    @Test
    public void nestedForeachMapped() throws InterruptedException {
        RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
            {
                Placeholder<ListOfStrings> someDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES));
                Placeholder<ListOfStrings> moreDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES_2));

                root("foreachDemo").foreach(someDevices).map(DemoContext::new).branch("workOnDeviceBranch")
                        .as((doWithDeviceBranch, devicePlaceholder) -> {
                            doWithDeviceBranch.leaf("Do something with device ").runCtx(device -> {
                                System.out.println(device);
                            });
                            doWithDeviceBranch.foreach(moreDevices).map(DemoContext::new)
                                    .branch("workOnDeviceAndNestedDeviceBranch")
                                    .as((nestedBranch, nestedDevicePlaceholder) -> {
                                        nestedBranch.leaf("Do something with both devices").runCtx(
                                                RunnableLeafsMoIeForeachIntegrationTest::doWithDevices,
                                                nestedDevicePlaceholder);
                                    });
                        });
            }
        }.build();
        Mole mole = new RunnableLeafsMole(Sets.newHashSet(mission));
        Map<String, Object> params = new HashMap<>();
        params.put(PARAMETER_NAME_DEVICE_NAMES, ITEM_LIST);
        params.put(PARAMETER_NAME_DEVICE_NAMES_2, ITEM_LIST_2);
        MissionHandle handle = mole.instantiate(new Mission("foreachDemo"), params).block();
        Thread.sleep(50);
        mole.instructRoot(handle, StrandCommand.RESUME);
        mole.statesFor(handle).blockLast();
    }

    @Test
    public void nestedForeachMappedInContextual() throws InterruptedException {
        RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
            {
                Placeholder<ListOfStrings> someDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES));
                Placeholder<ListOfStrings> moreDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES_2));
                Placeholder<String> contextParameterPlaceholder = Placeholder.aString("demoContextParameter");

                root("foreachDemo").foreach(someDevices).map(DemoContext::new).branch("workOnDeviceBranch")
                        .as((doWithDeviceBranch, devicePlaceholder) -> {
                            doWithDeviceBranch.leaf("Do something with device ").runCtx(device -> {
                                System.out.println(device);
                            });
                            doWithDeviceBranch.branch("context").contextual(String::new, contextParameterPlaceholder)
                                    .as((ctxBranch, ctxPlaceholder) -> {
                                        ctxBranch.foreach(moreDevices).map(DemoContext::new)
                                                .branch("workOnDeviceAndNestedDeviceBranch")
                                                .as((nestedBranch, nestedDevicePlaceholder) -> {
                                                    nestedBranch.leaf("Do something with both devices").runCtx(
                                                            RunnableLeafsMoIeForeachIntegrationTest::doWithDevices,
                                                            nestedDevicePlaceholder);
                                                    nestedBranch.leaf("Do ...").run((device, nestedDevice, ctx) -> {
                                                        System.out.println(device + " " + nestedDevice + " " + ctx);
                                                    }, devicePlaceholder, nestedDevicePlaceholder, ctxPlaceholder);
                                                });
                                    });
                        });
            }
        }.build();
        Mole mole = new RunnableLeafsMole(Sets.newHashSet(mission));
        Map<String, Object> params = new HashMap<>();
        params.put(PARAMETER_NAME_DEVICE_NAMES, ITEM_LIST);
        params.put(PARAMETER_NAME_DEVICE_NAMES_2, ITEM_LIST_2);
        params.put("demoContextParameter", "SomeText");
        MissionHandle handle = mole.instantiate(new Mission("foreachDemo"), params).block();
        Thread.sleep(50);
        mole.instructRoot(handle, StrandCommand.RESUME);
        mole.statesFor(handle).blockLast();
    }

    @Test
    public void nestedForeachAndContexts2() throws InterruptedException {
        RunnableLeafsMission mission = new RunnableLeafsMissionSupport() {
            {
                Placeholder<ListOfStrings> someDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES));
                Placeholder<ListOfStrings> moreDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES_2));

                root("foreachDemo").foreach(someDevices).map(DemoContext::new).branch("workOnDeviceBranch")
                        .as((doWithDeviceBranch, ctx) -> {
                            doWithDeviceBranch.leaf("Do something with device ").runCtx(device -> {
                                System.out.println(device);
                            });
                            doWithDeviceBranch.foreach(moreDevices).map(DemoContext::new)
                                    .branch("workOnDeviceAndNestedDeviceBranch").as((nestedBranch, nestedItem) -> {
                                        /**
                                         * Do something on both contexts.
                                         */
                                        nestedBranch.leaf("").run(
                                                RunnableLeafsMoIeForeachIntegrationTest::doWithDevices, ctx,
                                                nestedItem);
                                    });
                        });
            }
        }.build();
        Mole mole = new RunnableLeafsMole(Sets.newHashSet(mission));
        Map<String, Object> params = new HashMap<>();
        params.put(PARAMETER_NAME_DEVICE_NAMES, ITEM_LIST);
        params.put(PARAMETER_NAME_DEVICE_NAMES_2, ITEM_LIST_2);
        MissionHandle handle = mole.instantiate(new Mission("foreachDemo"), params).block();
        Thread.sleep(50);
        mole.instructRoot(handle, StrandCommand.RESUME);
        mole.statesFor(handle).blockLast();
    }

    private static void doWithDevices(DemoContext context1, DemoContext context2) {
        System.out.println("Do something with" + context1 + " and " + context2);
    }

    private static RunnableLeafsMission runCtxForDemo() {
        return new RunnableLeafsMissionSupport() {
            {
                Placeholder<String> contextParameterPlaceholder = Placeholder.aString("demoContextParameter");
                Placeholder<ListOfStrings> someDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES));
                Placeholder<ListOfStrings> moreDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES_2));

                root("runCtxForDemo").contextual(DemoContext::new, contextParameterPlaceholder)
                        .as((root, ctxPlaceholder) -> {
                            root.leaf("")
                                    .runCtx(demoContext -> System.out.println("context:" + demoContext.toString()));
                            root.foreach(someDevices).branch("runCtxForDemoBranch").as((branchDe, itemPlaceholder) -> {
                                branchDe.leaf("").run((demoContext, item) -> {
                                    System.out.println("out " + demoContext + " " + item);
                                }, ctxPlaceholder, itemPlaceholder);
                                /*
                                 * runCtx mixed implicit item and given placeholders seems counter intuitive!
                                 */
                                branchDe.leaf("leaf").runCtx((String item, DemoContext demoContext) -> {
                                    System.out.println("out " + demoContext + " " + item);
                                }, ctxPlaceholder);
                                branchDe.leaf("leaf").runCtx((item, in, out) -> {
                                    DemoContext demoContext = in.get(ctxPlaceholder);
                                    System.out.println("out " + demoContext + " " + item);
                                    out.emit("hello", "world");
                                });
                                branchDe.foreach(moreDevices).map(item -> new String(item)).branch("som")
                                        .as((branchDescription, branchDeItem) -> {
                                            branchDescription.branch("").as(branchDescriptionsd -> {
                                                branchDescriptionsd.leaf("").runCtx(name -> {
                                                    System.out.println("hello " + name);
                                                });
                                            });
                                            branchDescription.leaf("").runCtx((contextVal, itemVal) -> System.out
                                                    .println("item" + itemVal + "context: " + contextVal));
                                            branchDescription.foreach(moreDevices).branch("")
                                                    .as((branch12, branch12Device) -> {
                                                        branch12.leaf("a").run(in -> {
                                                            String outer = in.get(branchDeItem);
                                                            String inner = in.get(branch12Device);
                                                            System.out.println(outer + "+" + inner);
                                                        });
                                                    });
                                        });
                            });

                        });
            }
        }.build();

    }

    private static RunnableLeafsMission contextualMission() {

        return new RunnableLeafsMissionSupport() {
            {

                Placeholder<String> contextParameterPlaceholder = Placeholder.aString("demoContextParameter");

                Placeholder<ListOfStrings> someDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES));
                Placeholder<ListOfStrings> moreDevices = mandatory(
                        Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES_2));

                executionStrategy().defaultsTo(ExecutionStrategy.ABORT_ON_ERROR);

                root("foreachDemo").contextual(DemoContext::new, contextParameterPlaceholder).sequential()
                        .as((missionRoot, ctx) -> {

                            missionRoot.foreach(someDevices).parallel().leaf("switchOn").runCtx((String item) -> {
                                System.out.println("switchOn: " + item);
                            });

                            missionRoot.foreach(someDevices).parallel().branch("configure").sequential()
                                    .as((branchDescription, itemPlaceholder) -> {
                                        branchDescription.leaf("setValue").run((context, item) -> {

                                            System.out.println("setValue of " + item + " to xy, context:" + context);
                                            try {
                                                Thread.sleep(2000);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            System.out.println("setValue finished");

                                        }, ctx, itemPlaceholder);

                                        branchDescription.foreach(moreDevices).leaf("doSomethingInNestedLoop")
                                                .runCtx((item, in, out) -> {
                                                    String outerItem = in.get(itemPlaceholder);
                                                    System.out.println("nestedTask " + item
                                                            + " in outerForeach for item " + outerItem);
                                                    out.emit("nestedItems", outerItem + ":" + item);
                                                });

                                        branchDescription.leaf("setAnotherValue").runCtx((String item) -> {
                                            System.out.println("setValue of " + item + " to xy");
                                            Thread.sleep(1000);
                                        });

                                        branchDescription.leaf("switchOff").runCtx((String item) -> {
                                            System.out.println("switchOff " + item);
                                            Thread.sleep(1000);
                                        });
                                    });
                        });
            }

        }.build();

    }

    private static Mole nonContextualMole() {
        return new RunnableLeafsMole(Sets.newHashSet(mission()));
    }

    private static Mole contextualMole() {
        return new RunnableLeafsMole(Sets.newHashSet(contextualMission()));
    }

    @Test
    public void justRunCtxFor() throws InterruptedException {
        Mole mole = new RunnableLeafsMole(Sets.newHashSet(runCtxForDemo()));
        Map<String, Object> params = new HashMap<>();
        params.put(PARAMETER_NAME_DEVICE_NAMES, ITEM_LIST);
        params.put(PARAMETER_NAME_DEVICE_NAMES_2, ITEM_LIST_2);
        params.put("demoContextParameter", "SomeText");
        params.put("deviecId", 4);
        MissionHandle handle = mole.instantiate(new Mission("runCtxForDemo"), params).block();
        Thread.sleep(50);
        mole.instructRoot(handle, StrandCommand.RESUME);

        mole.statesFor(handle).blockLast();
        MissionOutput output = mole.outputsFor(handle).blockLast();
        System.out.println(output);

    }

    @Test
    public void instantiateAndRunForeachInNonContextualBranches() throws InterruptedException {
        Mole mole = nonContextualMole();
        Map<String, Object> params = new HashMap<>();
        params.put(PARAMETER_NAME_DEVICE_NAMES, ITEM_LIST);
        params.put(PARAMETER_NAME_DEVICE_NAMES_2, ITEM_LIST_2);
        params.put("demoContextParameter", "SomeText");

        MissionHandle handle = mole.instantiate(new Mission("foreachDemo"), params).block(Duration.ofMillis(500));
        /*
         * tedious, this should maybe be worth an issue. At least a later convenience API should block
         * until mission can actually be started
         */
        Thread.sleep(50);

        mole.instructRoot(handle, StrandCommand.RESUME);
        MissionOutput output = mole.outputsFor(handle).blockLast();
        MissionRepresentation representation = mole.representationsFor(handle).blockLast();
        /*
         * TODO do we need a way to infer block id from mission-/treeStructure. One approach could be a hierarchical
         * blockIdSystem?
         * root.forEach.iteration0
         * root.main1.sub...
         */
        List<Block> forEachBlocks = representation
                .childrenOf(Block.builder("0.1.0", "foreachDemo_forEachItemIn:deviceNames_configure").build());
        System.out.println("foreachblocks" + forEachBlocks);
        for (int i = 0; i < ITEM_LIST.size(); i++) {
            for (int j = 0; j < ITEM_LIST_2.size(); j++) {
                String blockId = MessageFormat.format("0.1.{0}.1.{1}", i, j);
                Block block = representation.blockOfId(blockId).get();
                String blockOutput = output.get(block, Placeholder.aString("nestedItems"));
                Assertions.assertThat(blockOutput).isEqualTo(ITEM_LIST.get(i) + ":" + ITEM_LIST_2.get(j));
            }

        }
        System.out.println(output);
        mole.statesFor(handle).blockLast();
    }

    @Test
    public void instantiateAndRunForeachInContextual() throws InterruptedException {
        Mole mole = contextualMole();
        Map<String, Object> params = new HashMap<>();
        params.put(PARAMETER_NAME_DEVICE_NAMES, ITEM_LIST);
        params.put(PARAMETER_NAME_DEVICE_NAMES_2, ITEM_LIST_2);
        params.put("demoContextParameter", "SomeText");

        MissionHandle handle = mole.instantiate(new Mission("foreachDemo"), params).block(Duration.ofMillis(500));
        /*
         * tedious, this should maybe be worth an issue. At least a later convenience API should block
         * until mission can actually be started
         */
        Thread.sleep(50);

        mole.instructRoot(handle, StrandCommand.RESUME);
        MissionOutput output = mole.outputsFor(handle).blockLast();
        MissionRepresentation representation = mole.representationsFor(handle).blockLast();
        /*
         * TODO do we need a way to infer block id from mission-/treeStructure. One approach could be a hierarchical
         * blockIdSystem?
         * root.forEach.iteration0
         * root.main1.sub...
         */
        List<Block> forEachBlocks = representation
                .childrenOf(Block.builder("0.1.0", "foreachDemo_forEachItemIn:deviceNames_configure").build());
        System.out.println("foreachblocks" + forEachBlocks);
        for (int i = 0; i < ITEM_LIST.size(); i++) {
            for (int j = 0; j < ITEM_LIST_2.size(); j++) {
                String blockId = MessageFormat.format("0.1.{0}.1.{1}", i, j);
                Block block = representation.blockOfId(blockId).get();
                String blockOutput = output.get(block, Placeholder.aString("nestedItems"));
                Assertions.assertThat(blockOutput).isEqualTo(ITEM_LIST.get(i) + ":" + ITEM_LIST_2.get(j));
            }

        }
        System.out.println(output);
        mole.statesFor(handle).blockLast();
    }

    private static class DemoContext {

        private String text;

        public DemoContext(String text) {
            this.text = "Contextual" + text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

}
