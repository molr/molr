/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package io.molr.commons.domain;

import com.google.common.collect.*;

import java.util.*;

import static java.util.Objects.requireNonNull;

public final class MissionState {
    private final Result result;
    private final Strand rootStrand;
    private final SetMultimap<Strand, StrandCommand> strandAllowedCommands;
    private final Map<Strand, String> strandCursorBlockIds;
    private final Map<Strand, RunState> strandRunStates;
    private final ImmutableListMultimap<Strand, Strand> parentToChildren;
    private final Map<String, Result> blockIdsToResult;
    private final Map<String, RunState> blockIdsToRunState;
    private final Set<String> breakpointBlockIds;
    private final SetMultimap<String, BlockCommand> allowedBlockCommands;
    private final Set<MissionCommand> allowedMissionCommands;


    private MissionState(Builder builder) {
        this.rootStrand = requireNonNull(builder.rootStrand, "rootStrand must not be null");

        this.result = builder.result;
        /* Consider checking that the tree is consistent (everything has to be connected to the root)*/
        this.parentToChildren = builder.parentToChildrenBuilder.build();
        this.strandAllowedCommands = builder.strandAllowedCommandsBuilder.build();
        this.strandCursorBlockIds = builder.strandCursorBlockIdsBuilder.build();
        this.strandRunStates = builder.strandRunStatesBuilder.build();
        this.blockIdsToResult = builder.blockIdsToResult.build();
        this.blockIdsToRunState = builder.blockIdsToRunState.build();
        this.breakpointBlockIds = builder.breakpointBlockIds.build();
        this.allowedBlockCommands = builder.blocksToAllowedCommandsBuilder.build();
        this.allowedMissionCommands = builder.allowedMissionCommands.build();
    }

    public Set<StrandCommand> allowedCommandsFor(Strand strand) {
        return this.strandAllowedCommands.get(strand);
    }

    public Optional<String> cursorBlockIdIn(Strand strand) {
        return Optional.ofNullable(this.strandCursorBlockIds.get(strand));
    }

    public RunState runStateOf(Strand strand) {
        return this.strandRunStates.get(strand);
    }

    public Map<String, Result> blockIdsToResult() {
        return this.blockIdsToResult;
    }

    public Map<String, RunState> blockIdsToRunState() {
        return this.blockIdsToRunState;
    }

    public Result resultOf(Block block) {
        return resultOfBlockId(block.id());
    }

    public Result resultOfBlockId(String blockId) {
        return Optional.ofNullable(blockIdsToResult.get(blockId)).orElse(Result.UNDEFINED);
    }

    public Result result() {
        return this.result;
    }

    public RunState runStateOf(Block block) {
        return runStateOfBlockId(block.id());
    }

    public RunState runStateOfBlockId(String blockId) {
        return Optional.ofNullable(blockIdsToRunState.get(blockId)).orElse(RunState.NOT_STARTED);
    }

    public Strand rootStrand() {
        return this.rootStrand;
    }

    public RunState runState() {
        return runStateOf(this.rootStrand);
    }

    public List<Strand> childrenOf(Strand parent) {
        return this.parentToChildren.get(parent);
    }

    public Set<Strand> allStrands() {
        return strandRunStates.keySet();
    }

    public Set<String> breakpointBlockIds() {
        return this.breakpointBlockIds;
    }
    
    public Set<BlockCommand> allowedBlockCommandsFor(String blockId){
        return allowedBlockCommands.get(blockId);
    }
    
    public Set<BlockCommand> allowedBlockCommandsFor(Block block){
        return allowedBlockCommands.get(block.id());
    }
    
    public Set<MissionCommand> allowedMissionCommands(){
        return allowedMissionCommands;
    }
    
    public Map<String, Collection<BlockCommand>> blockIdsToAllowedCommands(){
        return allowedBlockCommands.asMap();
    }

    public static final Builder builder(Result result) {
        return new Builder(result);
    }

    public static class Builder {
        private final Result result;
        private Strand rootStrand;
        private final ImmutableSetMultimap.Builder<Strand, StrandCommand> strandAllowedCommandsBuilder = ImmutableSetMultimap.builder();
        private final ImmutableMap.Builder<Strand, String> strandCursorBlockIdsBuilder = ImmutableMap.builder();
        private final ImmutableMap.Builder<Strand, RunState> strandRunStatesBuilder = ImmutableMap.builder();
        private final ImmutableListMultimap.Builder<Strand, Strand> parentToChildrenBuilder = ImmutableListMultimap.builder();
        private final ImmutableMap.Builder<String, Result> blockIdsToResult = ImmutableMap.builder();
        private final ImmutableMap.Builder<String, RunState> blockIdsToRunState = ImmutableMap.builder();
        private final ImmutableSet.Builder<String> breakpointBlockIds = ImmutableSet.builder();
        private final ImmutableSetMultimap.Builder<String, BlockCommand> blocksToAllowedCommandsBuilder = ImmutableSetMultimap.builder();
        private final ImmutableSet.Builder<MissionCommand> allowedMissionCommands = ImmutableSet.builder();

        private Builder(Result result) {
            this.result = Objects.requireNonNull(result, "overall result must not be null");
        }

        public Builder add(Strand strand, RunState runState, Block cursor, Strand parent, Set<StrandCommand> allowedCommands) {
            String cursorBlockId = Optional.ofNullable(cursor).map(Block::id).orElse(null);
            return add(strand, runState, cursorBlockId, parent, allowedCommands);
        }

        public Builder add(Strand strand, RunState runState, String cursorBlockId, Strand parent, Set<StrandCommand> allowedCommands) {
            requireNonNull(strand, "strand must not be null");
            requireNonNull(runState, "runState must not be null");
            /* cursor might be null! */
            /* parent might be null! */

            if (parent == null) {
                if (rootStrand != null) {
                    throw new IllegalArgumentException("root Strand (strand without a parent) already set! Cannot set twice!");
                }
                this.rootStrand = strand;
            } else {
                parentToChildrenBuilder.put(parent, strand);
            }

            if (cursorBlockId != null) {
                strandCursorBlockIdsBuilder.put(strand, cursorBlockId);
            }

            strandRunStatesBuilder.put(strand, runState);
            strandAllowedCommandsBuilder.putAll(strand, allowedCommands);
            return this;
        }

        public Builder add(Strand strand, RunState runState, Block cursor, Strand parent, StrandCommand... allowedCommands) {
            return this.add(strand, runState, cursor, parent, ImmutableSet.copyOf(allowedCommands));
        }

        public Builder add(Strand strand, RunState runState, Block cursor, Set<StrandCommand> allowedCommands) {
            return this.add(strand, runState, cursor, null, allowedCommands);
        }

        public Builder blockResult(String blockId, Result result) {
            blockIdsToResult.put(blockId, result);
            return this;
        }

        public Builder blockResult(Block block, Result result) {
            return blockResult(block.id(), result);
        }

        public Builder blockRunState(String blockId, RunState runState) {
            blockIdsToRunState.put(blockId, runState);
            return this;
        }

        public Builder blockRunState(Block block, RunState runState) {
            return blockRunState(block.id(), runState);
        }

        public Builder addBreakpoint(Block block) {
            this.breakpointBlockIds.add(block.id());
            return this;
        }
        
        public Builder addBreakpoint(String blockId) {
            this.breakpointBlockIds.add(blockId);
            return this;
        }      
        
        public Builder addAllowedCommand(Block block, BlockCommand command) {
            this.blocksToAllowedCommandsBuilder.put(block.id(), command);
            return this;
        }
        
        public Builder addAllowedCommand(String blockId, BlockCommand command) {
            this.blocksToAllowedCommandsBuilder.put(blockId, command);
            return this;
        }
        
        public Builder addAllowedCommand(MissionCommand command) {
            this.allowedMissionCommands.add(command);
            return this;
        }

        public MissionState build() {
            return new MissionState(this);
        }

    }

}
