package io.molr.commons.domain;

import java.util.Set;

public interface AgencyState {

    Set<Mission> executableMissions();

    Set<MissionInstance> activeMissions();

}
