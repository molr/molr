/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.request.server;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A request sent by MolR server to the supervisor to get its heartbeat
 *
 * @author yassine-kr
 */
public final class SupervisorHeartbeatRequest {

    /**
     * The interval between two states
     */
    private final int interval;

    public SupervisorHeartbeatRequest(@JsonProperty("interval") int interval) {
        this.interval = interval;
    }

    public int getInterval() {
        return interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return 0;
    }

}
