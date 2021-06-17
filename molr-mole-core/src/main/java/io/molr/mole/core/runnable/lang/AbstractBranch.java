package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MissionParameter;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;
import io.molr.mole.core.runnable.ContextConfiguration;
import io.molr.mole.core.runnable.ForEachConfiguration;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.utils.Checkeds;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractBranch {

    private final RunnableLeafsMission.Builder builder;
    private final Block parent;

    protected AbstractBranch(RunnableLeafsMission.Builder builder, Block parent) {
        this.builder = requireNonNull(builder, "builder must not be null");
        this.parent = requireNonNull(parent, "parent must not be null");
    }

    public abstract GenericOngoingBranch<? extends GenericOngoingBranch<?>> branch(String name, Placeholder<?>... placeholders);

    public abstract GenericOngoingLeaf<? extends GenericOngoingLeaf<?>> leaf(String name, Placeholder<?>... placeholders);

    protected RunnableLeafsMission.Builder builder() {
        return this.builder;
    }

    protected Block parent() {
        return this.parent;
    }

    public void integrate(RunnableLeafsMission otherMission) {
    	integrate(otherMission, Map.of());
    }

	public <T1> void integrate(RunnableLeafsMission simple, Placeholder<T1> key, Placeholder<T1> value) {
		integrate(simple, Map.of(key, value));
	}
    
	public <T1, T2> void integrate(RunnableLeafsMission simple, Placeholder<T1> key1, Placeholder<T1> value1,
			Placeholder<T1> key2, Placeholder<T1> value2) {
		integrate(simple, Map.of(key1, value1, key2, value2));
//		System.out.println(devP);
//		System.out.println("simple"+simple.treeStructure());
//		simple.parameterDescription().parameters().forEach(param->{
//			System.out.println(param.placeholder());
//			if(param.isRequired()) {
//				if(!param.placeholder().equals(placeholder)) {
//					throw new IllegalArgumentException("Missing required parameter.");
//				}
//			}
//		});
//		Map<Placeholder<?>, Placeholder<?>> mappings = new HashMap<>();
//		if(!placeholder.equals(devP)) {
//			mappings.put(placeholder, devP);
//		}
//		else {
//			System.out.println("WARN: non necessary mapping");
//			mappings.put(placeholder, devP);
//		}
//		integrate(simple, mappings);

	}
	
	/*
	 * Questions: allow non explicitly mapped placeholders?
	 */
	public void integrate(RunnableLeafsMission mission, Map<Placeholder<?>, Placeholder<?>> mappings) {
		Block rootRelica = addReplicatedTreeToParent(parent(), mission.treeStructure().rootBlock(), mission);
		System.out.println("reli: "+rootRelica+"\n");
		
		Set<Placeholder<?>> required = mission.parameterDescription().parameters()
				.stream().filter(MissionParameter::isRequired)
				.map(p->p.placeholder())
				.collect(Collectors.toSet());
		Assertions.assertThat(mappings.keySet()).containsAll(required);
		
		mission.parameterDescription().parameters().stream().filter(p->!p.isRequired() && !p.placeholder().equals(Placeholders.EXECUTION_STRATEGY)).forEach(p->{
			System.out.println("Optional parameter "+p+"\n\n");
			//throw new RuntimeException("Check for optional and default value?");
		});
		
		if(!mappings.isEmpty()) {
			builder().addBlockScope(rootRelica, mappings);
		}
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
    
    public void println(Object object) {
        leaf("println(\"" + object + "\");").run(() -> System.out.println(object));
    }

    public void sleep(long time, TimeUnit unit) {
        leaf("Sleep " + time + " " + unit).run(() -> unit.sleep(time));
    }

    /*
    DEPRECATED methods. To be removed asap.
     */

    @Deprecated
    public void run(String name, Runnable runnable) {
        leaf(name).run(runnable);
    }

    @Deprecated
    public void run(String name, Checkeds.CheckedThrowingRunnable runnable) {
        leaf(name).run(runnable);
    }

    @Deprecated
    public void run(String name, Checkeds.CheckedThrowingConsumer<In> runnable) {
        leaf(name).run(runnable);
    }

    @Deprecated
    public void run(String name, Checkeds.CheckedThrowingBiConsumer<In, Out> runnable) {
        leaf(name).run(runnable);
    }

}
