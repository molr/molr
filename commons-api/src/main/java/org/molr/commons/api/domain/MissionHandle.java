/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.api.domain;

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

}
