package io.molr.mole.core.runnable.lang;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.ContextConfiguration;
import io.molr.mole.core.runnable.ForEachConfiguration;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;

public abstract class ForeachBranchProvidingAbstractBranch extends AbstractBranch{

	protected ForeachBranchProvidingAbstractBranch(Builder builder, Block parent) {
		super(builder, parent);

	}
	
    public <T> ForeachBranchRoot<T> foreach(Placeholder<? extends MolrCollection<T>> itemsPlaceholder) {
    	requireNonNull(itemsPlaceholder);
    	String name = "forEachItemIn:"+itemsPlaceholder.name();
    	BlockNameConfiguration formatter = BlockNameConfiguration.builder().text(name).build();
        return new ForeachBranchRoot<>(formatter, builder(), parent(), BranchMode.SEQUENTIAL, itemsPlaceholder);
    }
    
	public void addMission(RunnableLeafsMission simple, Placeholder<String> placeholder, Placeholder<String> devP) {
		System.out.println(devP);
		System.out.println("simple"+simple.treeStructure());
		simple.parameterDescription().parameters().forEach(param->{
			System.out.println(param.placeholder());
			if(param.isRequired()) {
				if(!param.placeholder().equals(placeholder)) {
					//throw new IllegalArgumentException("Missing required parameter.");
				}
			}
//			MissionParameterDescription desc = in.get(param.placeholder());
		});
		Map<Placeholder<?>, Placeholder<?>> mappings = new HashMap<>();
		if(!placeholder.equals(devP)) {
			mappings.put(placeholder, devP);
		}
		else {
			System.out.println("WARN: non necessary mapping");
		}
		addMission(simple, mappings);

	}
	
	private void addMission(RunnableLeafsMission simple, Map<Placeholder<?>, Placeholder<?>> mappings) {
		Block rootRelica = addReplicatedTreeToParent(parent(), simple.treeStructure().rootBlock(), simple);
		System.out.println("reli: "+rootRelica+"\n");
		builder().addBlockScope(rootRelica, mappings);
		//builder().addBlockScope(rootRelica, Map.of(placeholder, devP));
	}
	
	private Block addReplicatedTreeToParent(Block parent, Block root, RunnableLeafsMission mission) {
		if(mission.treeStructure().isLeaf(root)) {
			System.out.println("add child of "+parent);
			return builder().leafChild(parent, BlockNameConfiguration.builder().text(root.text()).build(), mission.runnables().get(root), Set.of());
		}
		else {
			System.out.println("branch as child of "+parent);
			Block newParent = builder().childBranchNode(parent, BlockNameConfiguration.builder().text(root.text()).build(), SEQUENTIAL, Set.of());
			if(mission.forEachBlocksConfigurations().containsKey(root)) {
				ForEachConfiguration<?, ?> config = mission.forEachBlocksConfigurations().get(root);
				builder().forEachConfig(newParent, config);
				System.out.println(config.collectionPlaceholder());
			}
			if(mission.contexts().containsKey(root)) {
				System.out.println("ctxs"+mission.contexts());
				ContextConfiguration contextCfg = mission.contexts().get(root);
				System.out.println(contextCfg.contextPlaceholder());
				builder().addContextConfiguration(newParent, contextCfg);
			}
			mission.treeStructure().childrenOf(root).forEach(child->{
				System.out.println(child);
				addReplicatedTreeToParent(newParent, child, mission);
			});
			return newParent;
		}
		//System.out.println("foreach"+mission.forEachBlocksConfigurations());

	}


}
