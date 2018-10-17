package org.molr.commons.domain.dto;

import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.MissionInstance;

public class MissionInstanceDto {

    public final String mission;
    public final String handle;

    public MissionInstanceDto(String mission, String handle) {
        this.mission = mission;
        this.handle = handle;
    }

    public MissionInstanceDto() {
        this.mission = null;
        this.handle = null;
    }

    public static final MissionInstanceDto from(MissionInstance instance) {
        return new MissionInstanceDto(instance.mission().name(), instance.handle().id());
    }

    public MissionInstance toMissionInstance() {
        return new MissionInstance(MissionHandle.ofId(this.handle), new Mission(this.mission));
    }

    @Override
    public String toString() {
        return "MissionInstanceDto{" +
                "mission='" + mission + '\'' +
                ", handle='" + handle + '\'' +
                '}';
    }
}
