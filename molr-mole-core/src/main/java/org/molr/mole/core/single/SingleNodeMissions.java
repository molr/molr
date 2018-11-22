package org.molr.mole.core.single;

import org.molr.commons.domain.ImmutableMissionRepresentation;
import org.molr.commons.domain.MissionRepresentation;

public final class SingleNodeMissions {

    private SingleNodeMissions() {/* only static methods*/}

    public static final MissionRepresentation representationFor(SingleNodeMission mission) {
        return ImmutableMissionRepresentation.empty(mission.name());
    }
}
