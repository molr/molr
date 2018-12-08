package io.molr.mole.core.single;

import io.molr.commons.domain.ImmutableMissionRepresentation;
import io.molr.commons.domain.MissionRepresentation;

public final class SingleNodeMissions {

    private SingleNodeMissions() {/* only static methods*/}

    public static final MissionRepresentation representationFor(SingleNodeMission mission) {
        return ImmutableMissionRepresentation.empty(mission.name());
    }
}
