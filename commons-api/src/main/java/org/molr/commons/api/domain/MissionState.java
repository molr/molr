/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.api.domain;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class MissionState {
    private final SetMultimap<Strand, MissionCommand> strandAllowedCommands;
    private final Map<Strand, Block> strandCursorPositions;
    private final Map<Strand, RunState> strandRunStates;
    private final Set<Strand> activeStrands;

    private MissionState(Builder builder) {
        this.activeStrands = builder.activeStrandsBuilder.build();
        this.strandAllowedCommands = builder.strandAllowedCommandsBuilder.build();
        this.strandCursorPositions = builder.strandCursorPositionsBuilder.build();
        this.strandRunStates = builder.strandRunStatesBuilder.build();
    }

    public Set<MissionCommand> allowedCommandsFor(Strand strand) {
        return this.strandAllowedCommands.get(strand);
    }

    public Block cursorPositionIn(Strand strand) {
        return this.strandCursorPositions.get(strand);
    }

    public RunState runStateOf(Strand strand) {
        return this.strandRunStates.get(strand);
    }

    public Set<Strand> activeStrands() {
        return this.activeStrands;
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
                Objects.equals(activeStrands, that.activeStrands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(strandAllowedCommands, strandCursorPositions, strandRunStates, activeStrands);
    }

    @Override
    public String toString() {
        return "MissionState{" +
                "strandAllowedCommands=" + strandAllowedCommands +
                ", strandCursorPositions=" + strandCursorPositions +
                ", strandRunStates=" + strandRunStates +
                ", activeStrands=" + activeStrands +
                '}';
    }


    public static class Builder {
        private ImmutableSetMultimap.Builder<Strand, MissionCommand> strandAllowedCommandsBuilder = ImmutableSetMultimap.builder();
        private ImmutableMap.Builder<Strand, Block> strandCursorPositionsBuilder = ImmutableMap.builder();
        private ImmutableMap.Builder<Strand, RunState> strandRunStatesBuilder = ImmutableMap.builder();
        private ImmutableSet.Builder<Strand> activeStrandsBuilder = ImmutableSet.builder();

        private Builder() {
        }

        public Builder add(Strand strand, RunState runState, Block cursor, Set<MissionCommand> allowedCommands) {
            requireNonNull(strand, "strand must not be null");
            requireNonNull(runState, "runState must not be null");
            requireNonNull(cursor, "cursor must not be null");

            activeStrandsBuilder.add(strand);
            strandCursorPositionsBuilder.put(strand, cursor);
            strandRunStatesBuilder.put(strand, runState);
            strandAllowedCommandsBuilder.putAll(strand, allowedCommands);
            return this;
        }

        public Builder add(Strand strand, RunState runState, Block cursor, MissionCommand... allowedCommands) {
            return this.add(strand, runState, cursor, ImmutableSet.copyOf(allowedCommands));
        }

        public MissionState build() {
            return new MissionState(this);
        }

    }

}
