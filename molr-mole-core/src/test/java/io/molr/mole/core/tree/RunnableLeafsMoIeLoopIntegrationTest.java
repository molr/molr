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
import io.molr.commons.domain.Placeholders;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.RunnableLeafsMole;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;

/**
 * @author krepp
 */
public class RunnableLeafsMoIeLoopIntegrationTest {

	//private final static Placeholder<String> ITEM_PLACEHOLDER = Placeholder.aString("aForEachLoop.deviceName");
    private final static List<String> ITEM_LIST = new ArrayList<>(Arrays.asList("A", "B", "C"));
    private final static List<String> ITEM_LIST_2 = new ArrayList<>(Arrays.asList("D", "E", "F"));
    private final static String PARAMETER_NAME_DEVICE_NAMES = "deviceNames";
    private final static String PARAMETER_NAME_DEVICE_NAMES_2 = "deviceNames2";
    
    
    RunnableLeafsMission mission() {

        return new RunnableLeafsMissionSupport() {
            {
            	
                Placeholder<ListOfStrings> someDevices = mandatory(Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES));
                Placeholder<ListOfStrings> moreDevices = mandatory(Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES_2));

                optional(Placeholders.EXECUTION_STRATEGY, ExecutionStrategy.ABORT_ON_ERROR.name());
                                
                root("foreachDemo").sequential().as(missionRoot -> {// 0
                	
                	missionRoot.foreach(someDevices).parallel().leaf("switchOn").runFor((String item)-> {
                		System.out.println("switchOn: "+item);
                	});

                	missionRoot.foreach(someDevices).parallel().branch("configure").sequential().as((branchDescription, itemPlaceholder)-> {
                		branchDescription.leaf("setValue").runFor((item) -> {
                			
                			System.out.println("setValue of "+item+" to xy");
                			try {
                    			Thread.sleep(2000);
                			}
                			catch(Exception e) {
                				e.printStackTrace();
                			}
                			System.out.println("setValue finished");
                			

                		});
                		
                		branchDescription.foreach(moreDevices).leaf("doSomethingInNestedLoop").runFor((item, in, out)->{//0.1.i.1.j
                			String outerItem = in.get(itemPlaceholder);
                			System.out.println("nestedTask "+item + " in outerForeach for item "+outerItem);
                			out.emit("nestedItems", outerItem+":"+item);
                		});
                		
                		branchDescription.leaf("setAnotherValue").runFor((String item) -> {//2
                			System.out.println("setValue of "+item+" to xy");
                			Thread.sleep(1000);
                		});
                		
                		branchDescription.leaf("switchOff").runFor((String item) -> {
                			System.out.println("switchOff "+item);
                			Thread.sleep(1000);
                		});
                	});
                });
            }
            
        }.build();

    }
    
    RunnableLeafsMission contextualMission() {

        return new RunnableLeafsMissionSupport() {
            {

            	Placeholder<String> contextParameterPlaceholder = Placeholder.aString("demoContextParameter");
            	
                Placeholder<ListOfStrings> someDevices = mandatory(Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES));
                Placeholder<ListOfStrings> moreDevices = mandatory(Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES_2));

                optional(Placeholders.EXECUTION_STRATEGY, ExecutionStrategy.ABORT_ON_ERROR.name());
                                
                root("foreachDemo").contextual(DemoContext::new, contextParameterPlaceholder).sequential().as(missionRoot -> {// 0
                	
                	missionRoot.foreach(someDevices).parallel().leaf("switchOn").runFor((String item)-> {
                		System.out.println("switchOn: "+item);
                	});

                	missionRoot.foreach(someDevices).parallel().branch("configure").sequential().as((branchDescription, itemPlaceholder)-> {
                		branchDescription.leaf("setValue").runForCtx((context, item) -> {
                			
                			System.out.println("setValue of "+item+" to xy, context:" + context);
                			try {
                    			Thread.sleep(2000);
                			}
                			catch(Exception e) {
                				e.printStackTrace();
                			}
                			System.out.println("setValue finished");
                			

                		});
                		
                		branchDescription.foreach(moreDevices).leaf("doSomethingInNestedLoop").runFor((item, in, out)->{//0.1.i.1.j
                			String outerItem = in.get(itemPlaceholder);
                			System.out.println("nestedTask "+item + " in outerForeach for item "+outerItem);
                			out.emit("nestedItems", outerItem+":"+item);
                		});
                		
                		branchDescription.leaf("setAnotherValue").runFor((String item) -> {//2
                			System.out.println("setValue of "+item+" to xy");
                			Thread.sleep(1000);
                		});
                		
                		branchDescription.leaf("switchOff").runFor((String item) -> {
                			System.out.println("switchOff "+item);
                			Thread.sleep(1000);
                		});
                	});
                });
            }
            
        }.build();

    }

    Mole nonContextualMole() {
        return new RunnableLeafsMole(Sets.newHashSet(mission()));
    }

    Mole contextualMole() {
        return new RunnableLeafsMole(Sets.newHashSet(contextualMission()));
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
         * TODO do we need a way to infer block id from mission-/treeStructure. One approach could be a hierarchical blockIdSystem?
         * root.forEach.iteration0
         * root.main1.sub...
         */
        List<Block> forEachBlocks = representation.childrenOf(Block.builder("0.1.0", "foreachDemo_forEachItemIn:deviceNames_configure").build());
        System.out.println("foreachblocks"+forEachBlocks);
        for (int i = 0; i < ITEM_LIST.size(); i++) {
        	for (int j = 0; j < ITEM_LIST_2.size(); j++) {
        		String blockId = MessageFormat.format("0.1.{0}.1.{1}", i, j);
        		Block block = representation.blockOfId(blockId).get();
        		String blockOutput = output.get(block, Placeholder.aString("nestedItems"));
        		Assertions.assertThat(blockOutput).isEqualTo(ITEM_LIST.get(i)+":"+ITEM_LIST_2.get(j));
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
         * TODO do we need a way to infer block id from mission-/treeStructure. One approach could be a hierarchical blockIdSystem?
         * root.forEach.iteration0
         * root.main1.sub...
         */
        List<Block> forEachBlocks = representation.childrenOf(Block.builder("0.1.0", "foreachDemo_forEachItemIn:deviceNames_configure").build());
        System.out.println("foreachblocks"+forEachBlocks);
        for (int i = 0; i < ITEM_LIST.size(); i++) {
        	for (int j = 0; j < ITEM_LIST_2.size(); j++) {
        		String blockId = MessageFormat.format("0.1.{0}.1.{1}", i, j);
        		Block block = representation.blockOfId(blockId).get();
        		String blockOutput = output.get(block, Placeholder.aString("nestedItems"));
        		Assertions.assertThat(blockOutput).isEqualTo(ITEM_LIST.get(i)+":"+ITEM_LIST_2.get(j));
			}

        }
        System.out.println(output);
        mole.statesFor(handle).blockLast();
    }
    
    private static class DemoContext {
    	
    	private String text;
    	
    	public DemoContext(String text) {
    		System.out.println(text);
    		this.text = text;
    	}
    	
    	@Override
    	public String toString() {
    		System.out.println(" toString " + text);
    		return text;
    	}
    }
    
}
