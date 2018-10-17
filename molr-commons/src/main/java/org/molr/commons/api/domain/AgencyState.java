package org.molr.commons.api.domain;

import java.util.Set;

public interface AgencyState {

    Set<Mission> executableMissions();

    Set<MissionInstance> activeMissions();

}
