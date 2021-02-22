package io.molr.mole.core.tree;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
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

                executionStrategy().allowAll().defaultsTo(ExecutionStrategy.ABORT_ON_ERROR);

                root("root1").sequential().as(missionRoot -> {

                    missionRoot.branch("main1").parallel().as(main1Root -> {

                        main1Root.leaf("main1Sub1").run(() -> {
                            System.out.println("main1Sub1");
                        });

                        main1Root.branch("main1Sub2").sequential().as(main1Sub2 -> {

                            main1Sub2.leaf("main1Sub2Sub1").run(() -> {
                                System.out.println("main1Sub2Task1");
                            });

                            main1Sub2.leaf("main1Sub2Sub2").run(() -> {
                                System.out.println("main1Sub2Task2");
                                throw new RuntimeException("error");
                            });

                            main1Sub2.leaf("main1Sub2Sub3").run(() -> {
                                System.out.println("main1Sub2Task3");
                            });
                        });

                        main1Root.leaf("main1Sub3").run(() -> {
                            System.out.println("main1Sub1");
                        });
                    });

                    missionRoot.leaf("main2").run((in, out) -> {
                        System.out.println("hello1");
                    });
                    missionRoot.leaf("main3").run((in, out) -> {
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
        MissionState latestState = mole.statesFor(handle).blockLast();
        MissionRepresentation instantiatedRepresentation = mole.representationsFor(handle).blockFirst();
        Map<String, Block> blocksByText = blocksByText(instantiatedRepresentation); 

        Assertions.assertThat(latestState.resultOf(representation.rootBlock())).isEqualTo(Result.FAILED);
        Assertions.assertThat(latestState.resultOfBlockId(blocksByText.get("main1").id())).isEqualTo(Result.FAILED);
        Assertions.assertThat(latestState.resultOfBlockId(blocksByText.get("main1Sub1").id())).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(latestState.resultOfBlockId(blocksByText.get("main1Sub2").id())).isEqualTo(Result.FAILED);
        Assertions.assertThat(latestState.resultOfBlockId(blocksByText.get("main1Sub2Sub1").id())).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(latestState.resultOfBlockId(blocksByText.get("main1Sub2Sub2").id())).isEqualTo(Result.FAILED);
        Assertions.assertThat(latestState.resultOfBlockId(blocksByText.get("main1Sub2Sub3").id())).isEqualTo(Result.UNDEFINED);
        Assertions.assertThat(latestState.resultOfBlockId(blocksByText.get("main1Sub3").id())).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(latestState.resultOfBlockId(blocksByText.get("main2").id())).isEqualTo(Result.UNDEFINED);
        Assertions.assertThat(latestState.resultOfBlockId(blocksByText.get("main3").id())).isEqualTo(Result.UNDEFINED);

        Assertions.assertThat(latestState.runStateOf(latestState.rootStrand())).isEqualTo(RunState.FINISHED);
        /**
         * TODO We cannot rely on runStateOfBlockId - summarizer does not defer the correct runState from children
         */
		/*
		 * Assertions.assertThat(lastState.runStateOfBlockId("0")).isEqualTo(RunState.
		 * PAUSED);
		 * Assertions.assertThat(lastState.runStateOfBlockId("1")).isEqualTo(RunState.
		 * PAUSED);
		 * Assertions.assertThat(lastState.runStateOfBlockId("2")).isEqualTo(RunState.
		 * FINISHED);
		 * Assertions.assertThat(lastState.runStateOfBlockId("3")).isEqualTo(RunState.
		 * PAUSED);
		 */
        Assertions.assertThat(latestState.runStateOfBlockId(blocksByText.get("main1Sub2Sub1").id())).isEqualTo(RunState.FINISHED);
        Assertions.assertThat(latestState.runStateOfBlockId(blocksByText.get("main1Sub2Sub2").id())).isEqualTo(RunState.FINISHED);
        Assertions.assertThat(latestState.runStateOfBlockId(blocksByText.get("main1Sub2Sub3").id())).isEqualTo(RunState.NOT_STARTED);
		/*
		 * Assertions.assertThat(lastState.runStateOfBlockId("7")).isEqualTo(RunState.
		 * UNDEFINED);
		 * Assertions.assertThat(lastState.runStateOfBlockId("8")).isEqualTo(RunState.
		 * UNDEFINED);
		 * Assertions.assertThat(lastState.runStateOfBlockId("9")).isEqualTo(RunState.
		 * UNDEFINED);
		 */

    }

    @Test
    public void runMissionAndProceedOnError() throws InterruptedException {

        Mission mission = new Mission("root1");
        Mole mole = testMoole();
        MissionRepresentation representation = mole.representationOf(mission).block();
        HashMap<String, Object> params = new HashMap<>();
        params.put(Placeholders.EXECUTION_STRATEGY.name(), ExecutionStrategy.PROCEED_ON_ERROR.name());
        MissionHandle handle = mole.instantiate(mission, params).block();

        /*
         * issues
         * after return of mission handle, executor is not necessarily ready to resume/start mission
         * mole should complete on error or if all missions have been finished
         */


        Thread.sleep(1000);
        
        MissionRepresentation instantiatedRepresentation = mole.representationsFor(handle).blockFirst();
        instantiatedRepresentation.allBlocks().stream().filter(block->block.text().equals("main1"));

        mole.instructRoot(handle, StrandCommand.RESUME);

        MissionState lastState = mole.statesFor(handle).blockLast();
        Map<String, Block> blocksByText = blocksByText(instantiatedRepresentation); 

        Assertions.assertThat(lastState.resultOf(representation.rootBlock())).isEqualTo(Result.FAILED);
        Assertions.assertThat(lastState.resultOfBlockId(blocksByText.get("main1").id())).isEqualTo(Result.FAILED);
        Assertions.assertThat(lastState.resultOfBlockId(blocksByText.get("main1Sub1").id())).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(lastState.resultOfBlockId(blocksByText.get("main1Sub2").id())).isEqualTo(Result.FAILED);
        Assertions.assertThat(lastState.resultOfBlockId(blocksByText.get("main1Sub2Sub1").id())).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(lastState.resultOfBlockId(blocksByText.get("main1Sub2Sub2").id())).isEqualTo(Result.FAILED);
        Assertions.assertThat(lastState.resultOfBlockId(blocksByText.get("main1Sub2Sub3").id())).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(lastState.resultOfBlockId(blocksByText.get("main1Sub3").id())).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(lastState.resultOfBlockId(blocksByText.get("main2").id())).isEqualTo(Result.SUCCESS);
        Assertions.assertThat(lastState.resultOfBlockId(blocksByText.get("main3").id())).isEqualTo(Result.SUCCESS);

        Assertions.assertThat(lastState.runStateOf(lastState.rootStrand())).isEqualTo(RunState.FINISHED);
    }
    
    private static Map<String, Block> blocksByText(MissionRepresentation represenation){
    	HashMap<String, Block> textsToBlock = new HashMap<>();
    	represenation.allBlocks().forEach(block -> {
    		if(textsToBlock.containsKey(block.text())) {
    			throw new IllegalArgumentException();
    		}
    		textsToBlock.put(block.text(), block);
    	});
    	return ImmutableMap.copyOf(textsToBlock);

    }

}
