package org.molr.commons.domain.dto;

import org.molr.commons.domain.Mission;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class MissionSetDto {

    public final Set<MissionDto> missionDtoSet;

    public MissionSetDto(){
        missionDtoSet = Collections.emptySet();
    }

    public MissionSetDto(Set<Mission> missions){
        missionDtoSet = missions.stream().map(MissionDto::from).collect(Collectors.toSet());
    }

    public Set<Mission> toMissionSet(){
        return this.missionDtoSet.stream().map(MissionDto::toMission).collect(Collectors.toSet());
    }

    public static MissionSetDto from(Set<Mission> missions){
        return new MissionSetDto(missions);
    }
}
