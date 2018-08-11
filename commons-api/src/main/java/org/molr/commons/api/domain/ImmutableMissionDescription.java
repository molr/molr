package org.molr.commons.api.domain;

import java.util.Objects;

public class ImmutableMissionDescription implements MissionDescription {

    private final Mission mission;
    private final Block root;

    public ImmutableMissionDescription(Mission mission, Block root) {
        this.mission = mission;
        this.root = root;
    }

    @Override
    public Mission mission() {
        return this.mission;
    }

    @Override
    public Block rootBlock() {
        return this.root;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableMissionDescription that = (ImmutableMissionDescription) o;
        return Objects.equals(mission, that.mission) &&
                Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mission, root);
    }
}
