package org.molr.commons.domain.dto;

import com.google.common.collect.ImmutableMap;
import org.molr.commons.domain.*;
import org.molr.commons.domain.RunState;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class MissionStateDto {

    public final Map<String, Set<String>> strandAllowedCommands;
    public final Map<String, BlockDto> strandCursorPositions;
    public final Map<String, String> strandRunStates;
    public final Map<String, List<String>> parentToChildrenStrands;
    public final Set<StrandDto> strands;
    public final Map<String, String> blockResults;

    private MissionStateDto(Map<String, Set<String>> strandAllowedCommands, Map<String, BlockDto> strandCursorPositions, Map<String, String> strandRunStates, Set<StrandDto> strands, Map<String, List<String>> parentToChildrenStrands, Map<String, String> blockResults) {
        this.strandAllowedCommands = strandAllowedCommands;
        this.strandCursorPositions = strandCursorPositions;
        this.strandRunStates = strandRunStates;
        this.strands = strands;
        this.parentToChildrenStrands = parentToChildrenStrands;
        this.blockResults = blockResults;
    }

    public MissionStateDto() {
        this(emptyMap(), emptyMap(), emptyMap(), emptySet(), emptyMap(), emptyMap());
    }

    public static final MissionStateDto from(MissionState missionState) {
        Set<Strand> allStrands = missionState.allStrands();
        Set<StrandDto> strandDtos = allStrands.stream().map(StrandDto::from).collect(toSet());
        Map<String, BlockDto> strandCursors = allStrands.stream()
                .filter(s -> missionState.cursorPositionIn(s).isPresent())
                .collect(toMap(Strand::id, s -> missionState.cursorPositionIn(s).map(BlockDto::from).get()));

        Map<String, String> runStates = allStrands.stream()
                .collect(toMap(Strand::id, s -> missionState.runStateOf(s).name()));

        Map<String, List<String>> parentToChildrenStrands = new HashMap<>();
        Map<String, Set<String>> allowedCommands = new HashMap<>();
        for (Strand strand : allStrands) {
            Set<StrandCommand> commands = missionState.allowedCommandsFor(strand);
            if (!commands.isEmpty()) {
                allowedCommands.put(strand.id(), commands.stream().map(StrandCommand::name).collect(toSet()));
            }

            List<Strand> children = missionState.childrenOf(strand);
            if (!children.isEmpty()) {
                parentToChildrenStrands.put(strand.id(), children.stream().map((Strand::id)).collect(toList()));
            }
        }


        Map<String, String> blockRunStates = missionState.blockIdsToResult().entrySet().stream()
                .collect(toImmutableMap(e -> e.getKey(), e -> e.getValue().name()));
        return new MissionStateDto(allowedCommands, strandCursors, runStates, strandDtos, parentToChildrenStrands, blockRunStates);
    }

    public MissionState toMissionState() {
        Map<String, Strand> idsToStrand = strands.stream().collect(toMap(s -> s.id, StrandDto::toStrand));
        MissionState.Builder builder = MissionState.builder();

        Map<String, String> childrenToParentStrandId = childToParent();
        for (StrandDto strandDto : strands) {
            Strand strand = strandDto.toStrand();
            Block block = ofNullable(strandCursorPositions.get(strandDto.id)).map(BlockDto::toBlock).orElse(null);
            RunState state = RunState.valueOf(strandRunStates.get(strandDto.id));

            Set<String> commandNames = ofNullable(strandAllowedCommands.get(strandDto.id)).orElse(emptySet());
            Set<StrandCommand> commands = commandNames.stream().map(StrandCommand::valueOf).collect(toSet());

            String parentStrandId = childrenToParentStrandId.get(strand.id());
            Strand parentStrand = parentStrandId == null ? null : idsToStrand.get(parentStrandId);
            builder.add(strand, state, block, parentStrand, commands);
        }

        blockResults.entrySet().forEach(e -> builder.blockResult(e.getKey(), Result.valueOf(e.getValue())));
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
                ", strands=" + strands +
                '}';
    }
}
