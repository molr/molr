package io.molr.mole.core.runnable;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionParameterDescription;
import io.molr.commons.domain.Out;
import io.molr.mole.core.tree.TreeStructure;

public class RunnableLeafsMole extends RunnableLeafsMoleBase {

    public RunnableLeafsMole(Set<RunnableLeafsMission> missions) {
        super(missions(missions), runnables(missions), treeStructure(missions), parameterDescription(missions));
    }

    private static Set<Mission> missions(Set<RunnableLeafsMission> missions) {
        requireNonNull(missions, "missions must not be null");
        return missions.stream().map(rlm -> new Mission(rlm.name())).collect(toSet());
    }

    private static Function<Mission, TreeStructure> treeStructure(Set<RunnableLeafsMission> newMissions) {
        return m -> missionMap(newMissions).get(m).treeStructure();
    }

    private static Function<Mission, MissionParameterDescription> parameterDescription(
            Set<RunnableLeafsMission> newMissions) {
        return m -> missionMap(newMissions).get(m).parameterDescription();
    }

    private static BiFunction<Mission, Map<String, Object>, Map<Block, BiConsumer<In, Out>>> runnables(
            Set<RunnableLeafsMission> newMissions) {
        return (m, p) -> missionMap(newMissions).get(m).runnables();
    }

    private static Map<Mission, RunnableLeafsMission> missionMap(Set<RunnableLeafsMission> newMissions) {
        return newMissions.stream().collect(toMap(m -> new Mission(m.name()), identity()));
    }

}
