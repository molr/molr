package io.molr.mole.core.runnable.lang;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;

import com.google.common.collect.ImmutableMap;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.BlockAttribute;
import io.molr.commons.domain.In;
import io.molr.commons.domain.MissionParameter;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.ContextConfiguration;
import io.molr.mole.core.runnable.ForEachConfiguration;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.utils.Checkeds;

public abstract class AbstractBranch {

    private final RunnableLeafsMission.Builder builder;
    private final Block parent;

    protected AbstractBranch(RunnableLeafsMission.Builder builder, Block parent) {
        this.builder = requireNonNull(builder, "builder must not be null");
        this.parent = requireNonNull(parent, "parent must not be null");
    }

    public abstract GenericOngoingBranch<? extends GenericOngoingBranch<?>> branch(String name);

    public abstract GenericOngoingLeaf<? extends GenericOngoingLeaf<?>> leaf(String name);

    public abstract GenericOngoingBranch<? extends GenericOngoingBranch<?>> branch(String name,
            Placeholder<?>... placeholders);

    public abstract GenericOngoingLeaf<? extends GenericOngoingLeaf<?>> leaf(String name,
            Placeholder<?>... placeholders);

    protected RunnableLeafsMission.Builder builder() {
        return this.builder;
    }

    protected Block parent() {
        return this.parent;
    }

    public void embed(RunnableLeafsMission otherMission) {
        embed(otherMission, Map.of());
    }

    public <T1> void embed(RunnableLeafsMission simple, Placeholder<T1> key, Placeholder<T1> value) {
        embed(simple, Map.of(key, value));
    }

    public <T1, T2> void embed(RunnableLeafsMission simple, Placeholder<T1> key1, Placeholder<T1> value1,
            Placeholder<T2> key2, Placeholder<T2> value2) {
        embed(simple, Map.of(key1, value1, key2, value2));
    }

    /*
     * Questions: allow non explicitly mapped placeholders?
     * In this case we would need parameter information which is not available at this point in order to ensure that
     * placeholders are present
     * or we remove checks
     * 
     * How to handle optional parameters? Default values (see also issue)
     */
    public void embed(RunnableLeafsMission otherMission, Map<Placeholder<?>, Placeholder<?>> mappings) {
        Block integratedRootNode = addIntegratedMissionTreeToParent(parent(), otherMission.treeStructure().rootBlock(),
                otherMission);

        Set<Placeholder<?>> requiredParameters = otherMission.parameterDescription().parameters().stream()
                .filter(MissionParameter::isRequired).map(p -> p.placeholder()).collect(Collectors.toSet());
        Assertions.assertThat(mappings.keySet()).containsAll(requiredParameters);
        //otherMission.parameterDescription().parameters().stream().filter(p->!p.isRequired() && !p.placeholder().equals(Placeholders.EXECUTION_STRATEGY)).forEach(p->{
        //});
        if (!mappings.isEmpty()) {
            ImmutableMap.Builder<Placeholder<?>, Function<In, ?>> mappingsBuilder = ImmutableMap.builder();
            mappings.forEach((var, val) -> {
                mappingsBuilder.put(var, in -> in.get(val));
            });
            builder().addBlockLetValues(integratedRootNode, mappingsBuilder.build());
        }
    }

    private Block addIntegratedMissionTreeToParent(Block parent, Block root, RunnableLeafsMission mission) {

        Set<BlockAttribute> blockAttributes = Set
                .copyOf(mission.treeStructure().missionRepresentation().blockAttributes().get(root));

        if (mission.treeStructure().isLeaf(root)) {
            return builder().leafChild(parent, BlockNameConfiguration.builder().text(root.text()).build(),
                    mission.runnables().get(root), blockAttributes);
        }

        BranchMode branchMode;
        if (mission.treeStructure().isParallel(root)) {
            int maxConcurrency = mission.maxConcurrency().get(root);
            branchMode = BranchMode.newParallel(maxConcurrency);
        } else {
            branchMode = BranchMode.SEQUENTIAL;
        }

        Block newParent = builder().childBranchNode(parent, BlockNameConfiguration.builder().text(root.text()).build(),
                branchMode, blockAttributes);

        if (mission.forEachBlocksConfigurations().containsKey(root)) {
            ForEachConfiguration<?, ?> config = mission.forEachBlocksConfigurations().get(root);
            builder().forEachConfig(newParent, config);
        }
        if (mission.contexts().containsKey(root)) {
            ContextConfiguration contextCfg = mission.contexts().get(root);
            builder().addContextConfiguration(newParent, contextCfg);
        }
        mission.treeStructure().childrenOf(root).forEach(child -> {
            addIntegratedMissionTreeToParent(newParent, child, mission);
        });
        return newParent;
    }

    public void println(Object object) {
        leaf("println(\"" + object + "\");").run(() -> System.out.println(object));
    }

    public void sleep(long time, TimeUnit unit) {
        leaf("Sleep " + time + " " + unit).run(() -> unit.sleep(time));
    }

    /*
     * DEPRECATED methods. To be removed asap.
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
