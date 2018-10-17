package org.molr.commons.domain;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class MissionInstance {

    private final MissionHandle handle;
    private final Mission mission;

    public MissionInstance(MissionHandle handle, Mission mission) {
        this.handle = requireNonNull(handle, "handle must not be null");
        this.mission = requireNonNull(mission, "mission must not be null");
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionInstance that = (MissionInstance) o;
        return Objects.equals(handle, that.handle) &&
                Objects.equals(mission, that.mission);
    }

    @Override
    public int hashCode() {
        return Objects.hash(handle, mission);
    }

    @Override
    public String toString() {
        return "MissionInstance{" +
                "handle=" + handle +
                ", mission=" + mission +
                '}';
    }

    public MissionHandle handle() {
        return this.handle;
    }

    public  Mission mission() {
        return this.mission;
    }

}
