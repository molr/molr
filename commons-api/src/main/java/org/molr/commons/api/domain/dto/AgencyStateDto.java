package org.molr.commons.api.domain.dto;

import org.molr.commons.api.domain.AgencyState;
import org.molr.commons.api.domain.ImmutableAgencyState;
import org.molr.commons.api.domain.MissionInstance;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class AgencyStateDto {

    public final Set<MissionInstanceDto> activeMissions;

    public AgencyStateDto(Set<MissionInstanceDto> activeMissions) {
        this.activeMissions = activeMissions;
    }

    public AgencyStateDto() {
        this.activeMissions = Collections.emptySet();
    }

    public static final AgencyStateDto from(AgencyState agencyState) {
        Set<MissionInstanceDto> instances = agencyState.activeMissions().stream().map(MissionInstanceDto::from).collect(toSet());
        return new AgencyStateDto(instances);
    }

    public AgencyState toAgencyState() {
        Set<MissionInstance> instances = this.activeMissions.stream().map(MissionInstanceDto::toMissionInstance).collect(toSet());
        return ImmutableAgencyState.of(instances);
    }

    @Override
    public String toString() {
        return "AgencyStateDto{" +
                "activeMissions=" + activeMissions +
                '}';
    }
}
