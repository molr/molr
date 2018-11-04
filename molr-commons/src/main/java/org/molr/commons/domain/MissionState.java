/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.domain;

import com.google.common.collect.*;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class MissionState {
    private final SetMultimap<Strand, StrandCommand> strandAllowedCommands;
    private final Map<Strand, Block> strandCursorPositions;
    private final Map<Strand, RunState> strandRunStates;
    private final ImmutableListMultimap<Strand, Strand> parentToChildren;

    private MissionState(Builder builder) {
        /* Consider checking that the tree is consistent (everything has to be connected to the root)*/
        this.parentToChildren = builder.parentToChildrenBuilder.build();
        this.strandAllowedCommands = builder.strandAllowedCommandsBuilder.build();
        this.strandCursorPositions = builder.strandCursorPositionsBuilder.build();
        this.strandRunStates = builder.strandRunStatesBuilder.build();
    }

    public Set<StrandCommand> allowedCommandsFor(Strand strand) {
        return this.strandAllowedCommands.get(strand);
    }

    public Optional<Block> cursorPositionIn(Strand strand) {
        return Optional.ofNullable(this.strandCursorPositions.get(strand));
    }

    public RunState runStateOf(Strand strand) {
        return this.strandRunStates.get(strand);
    }

    public Optional<Strand> rootStrand() {
        return allStrands().stream().filter(strand -> !parentToChildren.containsValue(strand)).findFirst();
    }

    public List<Strand> childrenOf(Strand parent) {
        return this.parentToChildren.get(parent);
    }

    public Set<Strand> allStrands() {
        return strandRunStates.keySet();
    }

    public static final Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionState that = (MissionState) o;
        return Objects.equals(strandAllowedCommands, that.strandAllowedCommands) &&
                Objects.equals(strandCursorPositions, that.strandCursorPositions) &&
                Objects.equals(strandRunStates, that.strandRunStates) &&
                Objects.equals(parentToChildren, that.parentToChildren);
    }

    @Override
    public int hashCode() {
        return Objects.hash(strandAllowedCommands, strandCursorPositions, strandRunStates, parentToChildren);
    }

    @Override
    public String toString() {
        return "MissionState{" +
                "strandAllowedCommands=" + strandAllowedCommands +
                ", strandCursorPositions=" + strandCursorPositions +
                ", strandRunStates=" + strandRunStates +
                ", parentToChildren=" + parentToChildren +
                '}';
    }

    public static class Builder {
        private final ImmutableSetMultimap.Builder<Strand, StrandCommand> strandAllowedCommandsBuilder = ImmutableSetMultimap.builder();
        private final ImmutableMap.Builder<Strand, Block> strandCursorPositionsBuilder = ImmutableMap.builder();
        private final ImmutableMap.Builder<Strand, RunState> strandRunStatesBuilder = ImmutableMap.builder();
        private final ImmutableListMultimap.Builder<Strand, Strand> parentToChildrenBuilder = ImmutableListMultimap.builder();

        private Builder() {
        }

        public Builder add(Strand strand, RunState runState, Block cursor, Strand parent, Set<StrandCommand> allowedCommands) {
            requireNonNull(strand, "strand must not be null");
            requireNonNull(runState, "runState must not be null");
            /* cursor might be null! */
            /* parent might be null! */

            if (parent != null) {
                parentToChildrenBuilder.put(parent, strand);
            }

            if (cursor != null) {
                strandCursorPositionsBuilder.put(strand, cursor);
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

        public MissionState build() {
            return new MissionState(this);
        }

    }

}
