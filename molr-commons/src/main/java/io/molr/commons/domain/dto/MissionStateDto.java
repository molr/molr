package io.molr.commons.domain.dto;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.molr.commons.domain.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;

public class MissionStateDto {

    public final String result;
    public final Map<String, Set<String>> strandAllowedCommands;
    public final Map<String, String> strandCursorBlockIds;
    public final Map<String, String> strandRunStates;
    public final Map<String, List<String>> parentToChildrenStrands;
    public final Set<StrandDto> strands;
    public final Map<String, String> blockResults;
    public final Map<String, String> blockRunStates;
    
    public final Map<String, Set<String>> blockIdAllowedCommands;
    public final Set<String> breakpoints;
    public final Set<String> ignoreBlocks;
    public final Set<String> allowedMissionCommands;

    private MissionStateDto(String result, Map<String, Set<String>> strandAllowedCommands, Map<String, String> strandCursorBlockIds, Map<String, String> strandRunStates, Set<StrandDto> strands, Map<String, List<String>> parentToChildrenStrands, Map<String, String> blockResults, Map<String, String> blockRunStates, Map<String, Set<String>> blockIdAllowedCommands, Set<String> breakpoints, Set<String> ignoreBlocks, Set<String> allowedMissionCommands) {
        this.result = result;
        this.strandAllowedCommands = strandAllowedCommands;
        this.strandCursorBlockIds = strandCursorBlockIds;
        this.strandRunStates = strandRunStates;
        this.strands = strands;
        this.parentToChildrenStrands = parentToChildrenStrands;
        this.blockResults = blockResults;
        this.blockRunStates = blockRunStates;
        this.blockIdAllowedCommands = blockIdAllowedCommands;
        this.breakpoints = breakpoints;
        this.ignoreBlocks = ignoreBlocks;
        this.allowedMissionCommands = allowedMissionCommands;
    }

    public MissionStateDto() {
        this(null, emptyMap(), emptyMap(), emptyMap(), emptySet(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptySet(), emptySet(), emptySet());
    }

    public static final MissionStateDto from(MissionState missionState) {
        Set<Strand> allStrands = missionState.allStrands();
        Set<StrandDto> strandDtos = allStrands.stream().map(StrandDto::from).collect(toSet());
        Map<String, String> strandCursors = allStrands.stream()
                .filter(s -> missionState.cursorBlockIdIn(s).isPresent())
                .collect(toMap(Strand::id, s -> missionState.cursorBlockIdIn(s).get()));

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

        Map<String, Set<String>> blockIdAllowedCommands = new HashMap<>();
        missionState.blockIdsToAllowedCommands().forEach((id, allowedCmds)->{
            blockIdAllowedCommands.put(id, ImmutableSet.copyOf(allowedCmds.stream().map(cmd -> cmd.name()).collect(Collectors.toSet())));
        });
        Set<String> breakpointBlockIds = missionState.breakpointBlockIds();
        Set<String> ignoreBlockIds = missionState.ignoreBlockIds();
        
        
        Set<String> allowedMissionCommands = missionState.allowedMissionCommands().stream().map(MissionCommand::name).collect(toSet());
              
        return new MissionStateDto(missionState.result().name(), allowedCommands, strandCursors, runStates, strandDtos, parentToChildrenStrands, toNameMap(missionState.blockIdsToResult()), toNameMap(missionState.blockIdsToRunState()), blockIdAllowedCommands, breakpointBlockIds, ignoreBlockIds, allowedMissionCommands);
    }

    private static <T extends Enum<T>> Map<String, String> toNameMap(Map<String, T> inMap) {
        return inMap.entrySet().stream()
                .collect(toMap(e -> e.getKey(), e -> e.getValue().name()));
    }

    public MissionState toMissionState() {
        Map<String, Strand> idsToStrand = strands.stream().collect(toMap(s -> s.id, StrandDto::toStrand));
        MissionState.Builder builder = MissionState.builder(Result.valueOf(result));

        Map<String, String> childrenToParentStrandId = childToParent();
        for (StrandDto strandDto : strands) {
            Strand strand = strandDto.toStrand();
            String cursorBlock = ofNullable(strandCursorBlockIds.get(strandDto.id)).orElse(null);
            RunState state = RunState.valueOf(strandRunStates.get(strandDto.id));

            Set<String> commandNames = ofNullable(strandAllowedCommands.get(strandDto.id)).orElse(emptySet());
            Set<StrandCommand> commands = commandNames.stream().map(StrandCommand::valueOf).collect(toSet());

            String parentStrandId = childrenToParentStrandId.get(strand.id());
            Strand parentStrand = parentStrandId == null ? null : idsToStrand.get(parentStrandId);
            builder.add(strand, state, cursorBlock, parentStrand, commands);
        }

        blockResults.entrySet().forEach(e -> builder.blockResult(e.getKey(), Result.valueOf(e.getValue())));
        blockRunStates.entrySet().forEach(e -> builder.blockRunState(e.getKey(), RunState.valueOf(e.getValue())));
        
        breakpoints.forEach(breakpoint -> builder.addBreakpoint(breakpoint));
        ignoreBlocks.forEach(ignoreBlock -> builder.addIgnoreBlock(ignoreBlock));
        blockIdAllowedCommands.forEach((blockId, commands) ->{
            commands.stream().forEach(command -> builder.addAllowedCommand(blockId, BlockCommand.valueOf(command)));
        });
        
        allowedMissionCommands.stream().map(MissionCommand::valueOf).forEach(builder::addAllowedCommand);
        
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
                "result='" + result + '\'' +
                ", strandAllowedCommands=" + strandAllowedCommands +
                ", strandCursorBlockIds=" + strandCursorBlockIds +
                ", strandRunStates=" + strandRunStates +
                ", parentToChildrenStrands=" + parentToChildrenStrands +
                ", strands=" + strands +
                ", blockResults=" + blockResults +
                ", blockRunStates=" + blockRunStates +
                ", blockIdAllowedCommands=" + blockIdAllowedCommands +
                ", breakpoints=" + breakpoints + 
                ", ignore=" + ignoreBlocks + 
                ", allowedMissionCommands=" + allowedMissionCommands +
                '}';
    }
}
