/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.request.server;

/**
 * A request sent by MolR server to the supervisor to get its state
 *
 * @author yassine-kr
 */
public final class SupervisorStateRequest {

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
