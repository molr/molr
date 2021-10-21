package io.molr.commons.domain;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public final class ImmutableAgencyState implements AgencyState {

    private final Set<Mission> executableMissions;
    private final Set<MissionInstance> activeMissions;

    private ImmutableAgencyState(Set<Mission> executableMissions, Iterable<MissionInstance> activeMissions) {
        requireNonNull(executableMissions, "availableMissions must not be null");
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

    public static final AgencyState combine(Object[] states) {
        Set<Mission> availableMissions = Arrays.stream(states).map(s -> (AgencyState) s).flatMap(s -> s.executableMissions().stream()).collect(toSet());
        Set<MissionInstance> instances = Arrays.stream(states).map(s -> (AgencyState) s).flatMap(s -> s.activeMissions().stream()).collect(toSet());
        return new ImmutableAgencyState(availableMissions, instances);
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
                "availableMissions=" + executableMissions +
                ", activeMissions=" + activeMissions +
                '}';
    }
}
