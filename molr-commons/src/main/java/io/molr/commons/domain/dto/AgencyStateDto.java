package io.molr.commons.domain.dto;

import io.molr.commons.domain.AgencyState;
import io.molr.commons.domain.ImmutableAgencyState;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionInstance;

import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class AgencyStateDto {

    public final Set<MissionDto> availableMissions;
    public final Set<MissionInstanceDto> activeMissions;

    public AgencyStateDto(Set<MissionDto> availableMissions, Set<MissionInstanceDto> activeMissions) {
        this.availableMissions = availableMissions;
        this.activeMissions = activeMissions;
    }

    public AgencyStateDto() {
        this.availableMissions = null;
        this.activeMissions = Collections.emptySet();
    }

    public static final AgencyStateDto from(AgencyState agencyState) {
        Set<MissionDto> missions = agencyState.executableMissions().stream().map(MissionDto::from).collect(toSet());
        Set<MissionInstanceDto> instances = agencyState.activeMissions().stream().map(MissionInstanceDto::from).collect(toSet());
        return new AgencyStateDto(missions, instances);
    }

    public AgencyState toAgencyState() {
        Set<Mission> missions = this.availableMissions.stream().map(MissionDto::toMission).collect(toSet());
        Set<MissionInstance> instances = this.activeMissions.stream().map(MissionInstanceDto::toMissionInstance).collect(toSet());
        return ImmutableAgencyState.of(missions, instances);
    }

    @Override
    public String toString() {
        return "AgencyStateDto{" +
                "availableMissions="+availableMissions+", activeMissions=" + activeMissions +
                '}';
    }
}
