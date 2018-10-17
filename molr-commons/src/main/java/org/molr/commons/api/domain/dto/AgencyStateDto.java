package org.molr.commons.api.domain.dto;

import org.molr.commons.api.domain.AgencyState;
import org.molr.commons.api.domain.ImmutableAgencyState;
import org.molr.commons.api.domain.Mission;
import org.molr.commons.api.domain.MissionInstance;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class AgencyStateDto {

    public final Set<MissionDto> executableMissions;
    public final Set<MissionInstanceDto> activeMissions;

    public AgencyStateDto(Set<MissionDto> executableMissions, Set<MissionInstanceDto> activeMissions) {
        this.executableMissions = executableMissions;
        this.activeMissions = activeMissions;
    }

    public AgencyStateDto() {
        this.executableMissions = null;
        this.activeMissions = Collections.emptySet();
    }

    public static final AgencyStateDto from(AgencyState agencyState) {
        Set<MissionDto> missions = agencyState.executableMissions().stream().map(MissionDto::from).collect(toSet());
        Set<MissionInstanceDto> instances = agencyState.activeMissions().stream().map(MissionInstanceDto::from).collect(toSet());
        return new AgencyStateDto(missions, instances);
    }

    public AgencyState toAgencyState() {
        Set<Mission> missions = this.executableMissions.stream().map(MissionDto::toMission).collect(toSet());
        Set<MissionInstance> instances = this.activeMissions.stream().map(MissionInstanceDto::toMissionInstance).collect(toSet());
        return ImmutableAgencyState.of(missions, instances);
    }

    @Override
    public String toString() {
        return "AgencyStateDto{" +
                "activeMissions=" + activeMissions +
                '}';
    }
}
