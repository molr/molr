package org.molr.mole.core.single;

import com.google.common.collect.ImmutableMap;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionParameterDescription;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.mole.core.tree.AbstractJavaMole;
import org.molr.mole.core.tree.MissionExecutor;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class SingleNodeMole extends AbstractJavaMole {

    private final Map<Mission, SingleNodeMission<?>> missions;

    public SingleNodeMole(Set<SingleNodeMission<?>> singleLeafMissions) {
        requireNonNull(singleLeafMissions, "missions must not be null.");
        this.missions = ImmutableMap.copyOf(singleLeafMissions.stream().collect(Collectors.toMap(m -> new Mission(m.name()), m -> m)));
    }

    @Override
    public Set<Mission> availableMissions() {
        return missions.keySet();
    }

    @Override
    public MissionRepresentation representationOf(Mission mission) {
        return Optional.ofNullable(missions.get(mission))
                .map(m -> SingleNodeMissions.representationFor(m))
                .orElseThrow(() -> new IllegalArgumentException("Mole cannot handle mission '" + mission + "'."));
    }


    @Override
    public MissionParameterDescription parameterDescriptionOf(Mission mission) {
        return Optional.ofNullable(missions.get(mission))
                .map(m -> m.parameterDescription())
                .orElseThrow(() -> new IllegalArgumentException("Mole cannot handle mission '" + mission + "'."));
    }

    @Override
    protected MissionExecutor instantiate(Mission mission, Map<String, Object> params) {
        SingleNodeMission<?> singleNodeMission = Optional.ofNullable(missions.get(mission))
                .orElseThrow(() -> new IllegalArgumentException("Mole cannot handle mission '" + mission + "'."));
        return new SingleNodeMissionExecutor<>(singleNodeMission, params);
    }

}
