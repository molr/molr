/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.api.domain;

import java.util.Objects;

public class MissionHandle {


    private final String id;

    private MissionHandle(String id) {
        this.id = id;
    }

    public static MissionHandle ofId(String id) {
        return new MissionHandle(id);
    }

    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return "MissionHandle [id=" + id + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionHandle that = (MissionHandle) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
