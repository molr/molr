package org.molr.mole.core.single;

import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionParameterDescription;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.mole.core.tree.AbstractJavaMole;
import org.molr.mole.core.tree.MissionExecutor;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.copyOf;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class SingleNodeMole extends AbstractJavaMole {

    private final Map<Mission, SingleNodeMission<?>> missions;

    public SingleNodeMole(Set<SingleNodeMission<?>> singleLeafMissions) {
        super(extractMissions(singleLeafMissions));
        this.missions = copyOf(singleLeafMissions.stream().collect(toMap(m -> new Mission(m.name()), m -> m)));
    }

    private static Set<Mission> extractMissions(Set<SingleNodeMission<?>> missions) {
        requireNonNull(missions, "missions must not be null.");
        return missions.stream().map(snm -> new Mission(snm.name())).collect(toSet());
    }

    @Override
    public MissionRepresentation missionRepresentationOf(Mission mission) {
        return Optional.ofNullable(missions.get(mission))
                .map(m -> SingleNodeMissions.representationFor(m))
                .orElseThrow(() -> new IllegalArgumentException("Mole cannot handle mission '" + mission + "'."));
    }


    @Override
    public MissionParameterDescription missionParameterDescriptionOf(Mission mission) {
        return Optional.ofNullable(missions.get(mission))
                .map(m -> m.parameterDescription())
                .orElseThrow(() -> new IllegalArgumentException("Mole cannot handle mission '" + mission + "'."));
    }

    @Override
    protected MissionExecutor executorFor(Mission mission, Map<String, Object> params) {
        SingleNodeMission<?> singleNodeMission = Optional.ofNullable(missions.get(mission))
                .orElseThrow(() -> new IllegalArgumentException("Mole cannot handle mission '" + mission + "'."));
        return new SingleNodeMissionExecutor<>(singleNodeMission, params);
    }

}
