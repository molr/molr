package org.molr.commons.domain;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ImmutableAgencyState implements AgencyState {

    private final Set<Mission> executableMissions;
    private final Set<MissionInstance> activeMissions;

    private ImmutableAgencyState(Set<Mission> executableMissions, Iterable<MissionInstance> activeMissions) {
        requireNonNull(executableMissions, "executableMissions must not be null");
        requireNonNull(activeMissions, "activeMissions must not be null");
        this.executableMissions = ImmutableSet.copyOf(executableMissions);
        this.activeMissions = ImmutableSet.copyOf(activeMissions);
    }

    public static ImmutableAgencyState of(Set<Mission> executableMissions, Iterable<MissionInstance> activeMissions) {
        return new ImmutableAgencyState(executableMissions, activeMissions);
    }

    @Override
    public Set<Mission> executableMissions() {
        return this.executableMissions;
    }

    @Override
    public Set<MissionInstance> activeMissions() {
        return this.activeMissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableAgencyState that = (ImmutableAgencyState) o;
        return Objects.equals(executableMissions, that.executableMissions) &&
                Objects.equals(activeMissions, that.activeMissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executableMissions, activeMissions);
    }

    @Override
    public String toString() {
        return "ImmutableAgencyState{" +
                "executableMissions=" + executableMissions +
                ", activeMissions=" + activeMissions +
                '}';
    }
}
