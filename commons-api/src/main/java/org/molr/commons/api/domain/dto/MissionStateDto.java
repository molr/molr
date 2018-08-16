package org.molr.commons.api.domain.dto;

import org.molr.commons.api.domain.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class MissionStateDto {

    public final Map<String, Set<String>> strandAllowedCommands;
    public final Map<String, BlockDto> strandCursorPositions;
    public final Map<String, String> strandRunStates;
    public final Set<StrandDto> activeStrands;

    public MissionStateDto(Map<String, Set<String>> strandAllowedCommands, Map<String, BlockDto> strandCursorPositions, Map<String, String> strandRunStates, Set<StrandDto> activeStrands) {
        this.strandAllowedCommands = strandAllowedCommands;
        this.strandCursorPositions = strandCursorPositions;
        this.strandRunStates = strandRunStates;
        this.activeStrands = activeStrands;
    }

    public MissionStateDto() {
        this.strandAllowedCommands = Collections.emptyMap();
        this.strandCursorPositions = Collections.emptyMap();
        this.strandRunStates = Collections.emptyMap();
        this.activeStrands = emptySet();
    }

    public static final MissionStateDto from(MissionState missionState) {
        Set<Strand> activeStrands = missionState.activeStrands();
        Set<StrandDto> strandDtos = activeStrands.stream().map(StrandDto::from).collect(toSet());
        Map<String, BlockDto> strandCursors = activeStrands.stream().collect(toMap(s -> s.id(), s -> BlockDto.from(missionState.cursorPositionIn(s))));
        Map<String, String> runStates = activeStrands.stream().collect(toMap(s -> s.id(), s -> missionState.runStateOf(s).name()));
        Map<String, Set<String>> allowedCommands = new HashMap<>();
        for (Strand strand : activeStrands) {
            Set<MissionCommand> commands = missionState.allowedCommandsFor(strand);
            if (!commands.isEmpty()) {
                allowedCommands.put(strand.id(), commands.stream().map(MissionCommand::name).collect(toSet()));
            }
        }
        return new MissionStateDto(allowedCommands, strandCursors, runStates, strandDtos);
    }

    public MissionState toMissionState() {
        MissionState.Builder builder = MissionState.builder();
        for (StrandDto strandDto : activeStrands) {
            Strand strand = strandDto.toStrand();
            Block cursor = strandCursorPositions.get(strandDto.id).toBlock();
            RunState runState = RunState.valueOf(strandRunStates.get(strandDto.id));

            Set<String> commandNames = Optional.ofNullable(strandAllowedCommands.get(strandDto.id)).orElse(emptySet());
            Set<MissionCommand> commands = commandNames.stream().map(MissionCommand::valueOf).collect(toSet());

            builder.add(strand, runState, cursor, commands);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return "MissionStateDto{" +
                "strandAllowedCommands=" + strandAllowedCommands +
                ", strandCursorPositions=" + strandCursorPositions +
                ", strandRunStates=" + strandRunStates +
                ", activeStrands=" + activeStrands +
                '}';
    }
}
