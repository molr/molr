package io.molr.mole.core.tree;

import java.util.HashMap;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionRepresentation;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.Placeholders;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.RunnableLeafsMole;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;

/**
 * @author krepp
 */
public class LenientModeIntegrationTest {

    RunnableLeafsMission mission() {

        return new RunnableLeafsMissionSupport() {
            {

                optional(Placeholders.EXECUTION_STRATEGY, ExecutionStrategy.ABORT_ON_ERROR.name());

                root("root1").sequential().as(missionRoot -> {// 0

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
                                throw new RuntimeException("error");
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

    Mole testMoole() {
        return new RunnableLeafsMole(Sets.newHashSet(mission()));
    }

    @Test
    public void runMissionAndAbortOnError() throws InterruptedException {

        Mission mission = new Mission("root1");
        Mole mole = testMoole();
        MissionRepresentation representation = mole.representationOf(mission).block();
        HashMap<String, Object> params = new HashMap<>();
        params.put(Placeholders.EXECUTION_STRATEGY.name(), ExecutionStrategy.ABORT_ON_ERROR.name());

        MissionHandle handle = mole.instantiate(mission, params).block();
        Thread.sleep(100);
        mole.instructRoot(handle, StrandCommand.RESUME);

        // Find criterion valid criterion to block
        // mole.statesFor(handle).map(missionState ->
        // missionState.runStateOf(missionState.rootStrand())).filter(RunState.RUNNING::equals)
        // .blockFirst();
        // mole.statesFor(handle).map(missionState ->
        // missionState.runStateOf(missionState.rootStrand())).filter(RunState.PAUSED::equals)
        // .blockFirst();
        Thread.sleep(1000);
        MissionState latestState = mole.statesFor(handle).blockFirst();

        Assertions.assertThat(latestState.resultOf(representation.rootBlock())).isEqualTo(Result.FAILED);
        Assertions.assertThat(latestState.resultOfBlockId("1")).isEqualTo(Result.FAILED);
        Assertions.assertThat(latestState.resultOfBlockId("2")).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(latestState.resultOfBlockId("3")).isEqualTo(Result.FAILED);
        Assertions.assertThat(latestState.resultOfBlockId("4")).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(latestState.resultOfBlockId("5")).isEqualTo(Result.FAILED);
        Assertions.assertThat(latestState.resultOfBlockId("6")).isEqualTo(Result.UNDEFINED);
        Assertions.assertThat(latestState.resultOfBlockId("7")).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(latestState.resultOfBlockId("8")).isEqualTo(Result.UNDEFINED);
        Assertions.assertThat(latestState.resultOfBlockId("9")).isEqualTo(Result.UNDEFINED);

        Assertions.assertThat(latestState.runStateOf(latestState.rootStrand())).isEqualTo(RunState.FINISHED);
        /**
         * TODO We cannot rely on runStateOfBlockId - summarizer does not defer the correct runState from children
         */
        // Assertions.assertThat(lastState.runStateOfBlockId("0")).isEqualTo(RunState.PAUSED);
        // Assertions.assertThat(lastState.runStateOfBlockId("1")).isEqualTo(RunState.PAUSED);
        // Assertions.assertThat(lastState.runStateOfBlockId("2")).isEqualTo(RunState.FINISHED);
        // Assertions.assertThat(lastState.runStateOfBlockId("3")).isEqualTo(RunState.PAUSED);
        Assertions.assertThat(latestState.runStateOfBlockId("4")).isEqualTo(RunState.FINISHED);
        Assertions.assertThat(latestState.runStateOfBlockId("5")).isEqualTo(RunState.FINISHED);
        Assertions.assertThat(latestState.runStateOfBlockId("6")).isEqualTo(RunState.UNDEFINED);
        // Assertions.assertThat(lastState.runStateOfBlockId("7")).isEqualTo(RunState.UNDEFINED);
        // Assertions.assertThat(lastState.runStateOfBlockId("8")).isEqualTo(RunState.UNDEFINED);
        // Assertions.assertThat(lastState.runStateOfBlockId("9")).isEqualTo(RunState.UNDEFINED);

    }

    @Test
    public void runMissionAndProceedOnError() throws InterruptedException {

        Mission mission = new Mission("root1");
        Mole mole = testMoole();
        MissionRepresentation representation = mole.representationOf(mission).block();
        //TODO remove on merge
        ListMultimap<Block, Block> blocks = representation.parentsToChildren();
        Block root = representation.rootBlock();
        List<Block> childrenOfRoot = blocks.get(root);
        for (Block child : childrenOfRoot) {
            System.out.println(child);
            if (blocks.containsKey(child)) {
                System.out.println("  " + blocks.get(child));
            }
        }
        //
        HashMap<String, Object> params = new HashMap<>();
        params.put(Placeholders.EXECUTION_STRATEGY.name(), ExecutionStrategy.PROCEED_ON_ERROR.name());
        MissionHandle handle = mole.instantiate(mission, params).block();
        //TODO remove on merge
        mole.statesFor(handle).subscribe(missionState -> {
            System.out.println("----");
            System.out.println("StateUpdate:");
            System.out.println("Results");
            System.out.println(missionState.result());
            System.out.println(missionState.blockIdsToResult());
            System.out.println("RunStates:");
            System.out.println(missionState.blockIdsToRunState());
            System.out.println("Strands:");
            System.out.println(missionState.allStrands());
            System.out.println("----");
        });

        /*
         * issues
         * after return of mission handle, executor is not necessarily ready to resume/start mission
         * mole should complete on error or if all missions have been finished
         */


        Thread.sleep(1000);

        mole.instructRoot(handle, StrandCommand.RESUME);

        // Thread.sleep(10000);
        // System.out.println(mole.representationOf(mission).block().parentsToChildren());

        MissionState lastState = mole.statesFor(handle).blockLast();

        Assertions.assertThat(lastState.resultOf(representation.rootBlock())).isEqualTo(Result.FAILED);
        Assertions.assertThat(lastState.resultOfBlockId("1")).isEqualTo(Result.FAILED);
        Assertions.assertThat(lastState.resultOfBlockId("2")).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(lastState.resultOfBlockId("3")).isEqualTo(Result.FAILED);
        Assertions.assertThat(lastState.resultOfBlockId("4")).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(lastState.resultOfBlockId("5")).isEqualTo(Result.FAILED);
        Assertions.assertThat(lastState.resultOfBlockId("6")).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(lastState.resultOfBlockId("7")).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(lastState.resultOfBlockId("8")).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(lastState.resultOfBlockId("9")).isEqualTo(Result.SUCCESS);

        Assertions.assertThat(lastState.runStateOf(lastState.rootStrand())).isEqualTo(RunState.FINISHED);
    }

}
