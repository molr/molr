package org.molr.commons.domain.dto;

import org.molr.commons.domain.Mission;

public class MissionDto {

    public final String name;

    public MissionDto(String name) {
        this.name = name;
    }

    public MissionDto() {
        this.name = null;
    }

    public static MissionDto from(Mission mission) {
        return new MissionDto(mission.name());
    }

    public Mission toMission() {
        return new Mission(this.name);
    }

    @Override
    public String toString() {
        return "MissionDto{" +
                "name='" + name + '\'' +
                '}';
    }
}
