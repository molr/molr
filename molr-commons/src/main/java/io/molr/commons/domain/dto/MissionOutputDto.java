package io.molr.commons.domain.dto;

import io.molr.commons.domain.MissionOutput;

import java.util.Collections;
import java.util.Map;

public class MissionOutputDto {

    public final Map<String, Map<String, Object>> blockOutputs;

    public MissionOutputDto() {
        this.blockOutputs = Collections.emptyMap();
    }

    public MissionOutputDto(Map<String, Map<String, Object>> blockOutputs) {
        this.blockOutputs = blockOutputs;
    }

    public static final MissionOutputDto from(MissionOutput missionOutput) {
        return new MissionOutputDto(missionOutput.content());
    }

    public MissionOutput toMissionOutput() {
        return MissionOutput.fromBlockIds(blockOutputs);
    }
}
