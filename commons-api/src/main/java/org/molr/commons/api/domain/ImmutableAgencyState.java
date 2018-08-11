package org.molr.commons.api.domain;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class ImmutableAgencyState implements AgencyState {

    private final Set<MissionInstance> activeMissions;

    private ImmutableAgencyState(Iterable<MissionInstance> activeMissions) {
        requireNonNull(activeMissions, "acttiveMissions must not be null");
        this.activeMissions = ImmutableSet.copyOf(activeMissions);
    }

    public static ImmutableAgencyState of(Iterable<MissionInstance> activeMissions) {
        return new ImmutableAgencyState(activeMissions);
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
        return Objects.equals(activeMissions, that.activeMissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeMissions);
    }
}
