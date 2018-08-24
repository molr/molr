package org.molr.commons.api.domain.dto;

import com.google.common.collect.ImmutableMap;
import org.molr.commons.api.domain.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class MissionStateDto {

    public final Map<String, Set<String>> strandAllowedCommands;
    public final Map<String, BlockDto> strandCursorPositions;
    public final Map<String, String> strandRunStates;
    public final Map<String, List<String>> parentToChildrenStrands;
    public final Set<StrandDto> activeStrands;
    public final String rootStrandId;

    public MissionStateDto(Map<String, Set<String>> strandAllowedCommands, Map<String, BlockDto> strandCursorPositions, Map<String, String> strandRunStates, Set<StrandDto> activeStrands, Map<String, List<String>> parentToChildrenStrands, String rootStrandId) {
        this.strandAllowedCommands = strandAllowedCommands;
        this.strandCursorPositions = strandCursorPositions;
        this.strandRunStates = strandRunStates;
        this.activeStrands = activeStrands;
        this.parentToChildrenStrands = parentToChildrenStrands;
        this.rootStrandId = rootStrandId;
    }

    public MissionStateDto() {
        this.strandAllowedCommands = Collections.emptyMap();
        this.strandCursorPositions = Collections.emptyMap();
        this.strandRunStates = Collections.emptyMap();
        this.activeStrands = emptySet();
        this.parentToChildrenStrands = Collections.emptyMap();
        this.rootStrandId = null;
    }

    public static final MissionStateDto from(MissionState missionState) {
        Set<Strand> activeStrands = missionState.activeStrands();
        Set<StrandDto> strandDtos = activeStrands.stream().map(StrandDto::from).collect(toSet());
        Map<String, BlockDto> strandCursors = activeStrands.stream().filter(s -> missionState.cursorPositionIn(s).isPresent()).collect(toMap(s -> s.id(), s -> missionState.cursorPositionIn(s).map(BlockDto::from).get()));
        Map<String, String> runStates = activeStrands.stream().collect(toMap(s -> s.id(), s -> missionState.runStateOf(s).name()));
        Map<String, List<String>> parentToChildrenStrands = new HashMap<>();
        Map<String, Set<String>> allowedCommands = new HashMap<>();
        for (Strand strand : activeStrands) {
            Set<MissionCommand> commands = missionState.allowedCommandsFor(strand);
            if (!commands.isEmpty()) {
                allowedCommands.put(strand.id(), commands.stream().map(MissionCommand::name).collect(toSet()));
            }

            List<Strand> children = missionState.childrenOf(strand);
            if (!children.isEmpty()) {
                parentToChildrenStrands.put(strand.id(), children.stream().map((Strand::id)).collect(toList()));
            }
        }
        return new MissionStateDto(allowedCommands, strandCursors, runStates, strandDtos, parentToChildrenStrands, missionState.rootStrand().id());
    }

    public MissionState toMissionState() {
        MissionState.Builder builder = MissionState.builder();
        Map<String, Strand> strands = activeStrands.stream().collect(toMap(s -> s.id, StrandDto::toStrand));

        ImmutableMap<String, String> childrenToParent = childToParent();
        Strand rootStrand = strands.get(rootStrandId);
        for (StrandDto strandDto : activeStrands) {
            Strand strand = strandDto.toStrand();
            Block cursor = Optional.ofNullable(strandCursorPositions.get(strandDto.id)).map(BlockDto::toBlock).orElse(null);
            RunState runState = RunState.valueOf(strandRunStates.get(strandDto.id));

            Set<String> commandNames = Optional.ofNullable(strandAllowedCommands.get(strandDto.id)).orElse(emptySet());
            Set<MissionCommand> commands = commandNames.stream().map(MissionCommand::valueOf).collect(toSet());

            if (rootStrand.equals(strand)) {
                builder.add(strand, runState, cursor, null, commands);
            } else {
                builder.add(strand, runState, cursor, strands.get(childrenToParent.get(strand)), commands);
            }
        }
        return builder.build();
    }

    private ImmutableMap<String, String> childToParent() {
        ImmutableMap.Builder<String, String> childrenToParentBuilder = ImmutableMap.builder();
        for (Map.Entry<String, List<String>> entry : this.parentToChildrenStrands.entrySet()) {
            for (String child : entry.getValue()) {
                String parent = entry.getKey();
                childrenToParentBuilder.put(child, parent);
            }
        }
        return childrenToParentBuilder.build();
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
