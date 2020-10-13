package io.molr.mole.core.tree;

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
import io.molr.commons.domain.MolrCollection;
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

    private final static Placeholder<String> ITEM_PLACEHOLDER = Placeholder.aString("aForEachLoop.deviceName");
    private final static List<String> ITEM_LIST = new ArrayList<>(Arrays.asList("A", "B", "C"));
    private final static List<String> ITEM_LIST_2 = new ArrayList<>(Arrays.asList("D", "E", "F"));
    private final static String PARAMETER_NAME_DEVICE_NAMES = "deviceNames";
    private final static String PARAMETER_NAME_DEVICE_NAMES_2 = "deviceNames2";
    
    RunnableLeafsMission mission() {

        return new RunnableLeafsMissionSupport() {
            {

                Placeholder<? extends MolrCollection<String>> collectionPlaceholder = mandatory(Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES));
                Placeholder<? extends MolrCollection<String>> secondCollectionPlaceholder = mandatory(Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES_2));

                optional(Placeholders.EXECUTION_STRATEGY, ExecutionStrategy.ABORT_ON_ERROR.name());
                                
                root("root1").sequential().as(missionRoot -> {// 0
                	
                	missionRoot.foreach(collectionPlaceholder, "configCollection").parallel().leaf("switchOn").runFor((String item)-> {
                		System.out.println("foreach: "+item);
                	});
                	
                	missionRoot.foreach(collectionPlaceholder, "configCollection2").parallel().branch("switchOnBranch").sequential().as((branchDescription, itemPlaceholder)-> {
                		branchDescription.leaf("powerOn").runFor((String item) -> {
                			
                			System.out.println("powerOn"+item);
                			try {
                    			Thread.sleep(2000);
                			}
                			catch(Exception e) {
                				e.printStackTrace();
                			}
                			System.out.println("slept");
                			

                		});
                		
                		branchDescription.foreach(collectionPlaceholder, "nested").leaf("nestedLeaf").runFor((String item)->{
                			System.out.println("itemNested "+item);
                		});
                		
                		branchDescription.leaf("set").runFor((String item) -> {
                			System.out.println("set"+item);
                			Thread.sleep(1000);
                		});
                		
                		branchDescription.leaf("switchOff").runFor((String item) -> {
                			System.out.println("switchOff"+item);
                			Thread.sleep(1000);
                		});
                	});
                	
					/*
					 * //alternative 1
					 * missionRoot.branch("newForEach").parallel().foreachItem(collectionPlaceholder
					 * ).branch("forDevice").as((foreachBranch, itemPlaceholder)->{
					 * foreachBranch.leaf("hel").run(in->{
					 * System.out.println("hello each"+in.get(itemPlaceholder)); }); //
					 * foreachBranch.branch("anotherFor").sequential().foreachItem(
					 * secondCollectionPlaceholder).branch("sec").as((nestedForeachBranch,
					 * foreachItem2)-> { // nestedForeachBranch.leaf("hel").run(in->{ //
					 * System.out.println("hello each"+in.get(foreachItem2) +
					 * in.get(itemPlaceholder)); // }); // });
					 * 
					 * });
					 * 
					 * //alternative 2 Placeholder<String> returnedItemPlaceholder =
					 * missionRoot.branch("test").forEach(collectionPlaceholder, (branchDescription,
					 * itemPlaceholder) -> { branchDescription.leaf("hello").run((in, out) -> {
					 * System.out.println("hello"+in.get(itemPlaceholder)); });
					 * 
					 * branchDescription.branch("forEachChild").sequential().as(forEachChild ->{
					 * forEachChild.leaf("hello").run((in, out)-> {
					 * System.out.println(in.get(itemPlaceholder)); }); });
					 * 
					 * //other alternatives
					 * //branchDescription.foreach("ConfigureCollectionOfItems").of(
					 * collectionPlaceholder).parallel().do().branch("ConfigureSingleMagnet"){...}|
					 * leaf("switchOnMagnet").run((item, in, out)-> {})|mission(...);
					 * //branchDescription.foreach("ConfigureMagnets").of(collectionPlaceholder).
					 * parallel().do().branch("ConfigureSingleMagnet"){...}|leaf("switchOnMagnet").
					 * run((item, in, out)-> {})
					 * 
					 * branchDescription.branch("NextForEach").forEach(collectionPlaceholder,
					 * (nextForEach, nextItemPlaceholder)->{
					 * nextForEach.branch("hello").sequential().as(hello->{
					 * hello.leaf("name").run((in, out)->{
					 * System.out.println(in.get(nextItemPlaceholder)); }); }); });
					 * 
					 * // branchDescription.leaf("hello").run((item)->{ //
					 * System.out.println("item"); // }); });
					 */
                	
					/*
					 * missionRoot.leafForEach("aForEachLoop", collectionPlaceholder,
					 * ITEM_PLACEHOLDER, (in, out) -> { String deviceName =
					 * in.get(ITEM_PLACEHOLDER); System.out.println("deviceName: " + deviceName);
					 * out.emit(ITEM_PLACEHOLDER, deviceName); });
					 * 
					 * missionRoot.leafForEach("a2ndForEachLoop", secondCollectionPlaceholder,
					 * ITEM_PLACEHOLDER, (in, out) -> { String deviceName =
					 * in.get(ITEM_PLACEHOLDER); System.out.println("deviceName: " + deviceName);
					 * out.emit(ITEM_PLACEHOLDER, deviceName); });
					 */
                });
            }
            
        }.build();

    }
    
    RunnableLeafsMission contextualMission() {

        return new RunnableLeafsMissionSupport() {
            {

            	Placeholder<String> device = Placeholder.aString("textToBePrinted");
            	
                Placeholder<ListOfStrings> collectionPlaceholder = mandatory(Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES));
                Placeholder<ListOfStrings> secondCollectionPlaceholder = mandatory(Placeholder.aListOfStrings(PARAMETER_NAME_DEVICE_NAMES_2));

                optional(Placeholders.EXECUTION_STRATEGY, ExecutionStrategy.ABORT_ON_ERROR.name());
                                
                root("root1").contextual(String::new, device).sequential().as(missionRoot -> {// 0
                	
                	missionRoot.foreach(collectionPlaceholder, "configCollection").parallel().leaf("switchOn").runFor((String item)-> {
                		System.out.println("foreach: "+item);
                	});
                	
                	missionRoot.foreach(collectionPlaceholder, "configCollection2").parallel().branch("switchOnBranch").sequential().as((branchDescription, itemPlaceholder)-> {
                		branchDescription.leaf("powerOn").runForCtx((context, item) -> {
                			
                			System.out.println("powerOn"+item+" " + context);
                			try {
                    			Thread.sleep(2000);
                			}
                			catch(Exception e) {
                				e.printStackTrace();
                			}
                			System.out.println("slept");
                			

                		});
                		
                		branchDescription.foreach(collectionPlaceholder, "nested").leaf("nestedLeaf").runFor((String item)->{
                			System.out.println("itemNested "+item);
                		});
                		
                		branchDescription.leaf("set").runFor((String item) -> {
                			System.out.println("set"+item);
                			Thread.sleep(1000);
                		});
                		
                		branchDescription.leaf("switchOff").runFor((String item) -> {
                			System.out.println("switchOff"+item);
                			Thread.sleep(1000);
                		});
                	});
                });
            }
            
        }.build();

    }
    

    Mole testMole() {
        //return new RunnableLeafsMole(Sets.newHashSet(mission()));
        return new RunnableLeafsMole(Sets.newHashSet(contextualMission()));
    }
    
    @Test
    public void instantiateAndRun() throws InterruptedException {
        Mole mole = testMole();
        Map<String, Object> params = new HashMap<>();
        params.put(PARAMETER_NAME_DEVICE_NAMES, ITEM_LIST);
        params.put(PARAMETER_NAME_DEVICE_NAMES_2, ITEM_LIST_2);
        params.put("textToBePrinted", "SomeText");

        MissionHandle handle = mole.instantiate(new Mission("root1"), params).block(Duration.ofMillis(500));
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
        List<Block> forEachBlocks = representation.childrenOf(Block.builder("3", "aForEachLoop").build());
        System.out.println("foreachblocks"+forEachBlocks);
        for (int i = 0; i < forEachBlocks.size(); i++) {
            Block block = forEachBlocks.get(i);
            String blockOutput = output.get(block, ITEM_PLACEHOLDER);
            Assertions.assertThat(blockOutput).isEqualTo(ITEM_LIST.get(i));
        }
        
        List<Block> forEachBlocks2 = representation.childrenOf(Block.builder("4", "a2ndForEachLoop").build());
        System.out.println("foreachblocks2"+forEachBlocks2);
        for (int i = 0; i < forEachBlocks2.size(); i++) {
            Block block = forEachBlocks2.get(i);
            String blockOutput = output.get(block, ITEM_PLACEHOLDER);
            Assertions.assertThat(blockOutput).isEqualTo(ITEM_LIST_2.get(i));
        }
        
        System.out.println(output);
        mole.statesFor(handle).blockLast();
    }

}
