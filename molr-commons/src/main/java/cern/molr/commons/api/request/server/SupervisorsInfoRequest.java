/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.request.server;

/**
 * A request sent to MolR server to get the supervisors info stream
 *
 * @author yassine-kr
 */
public final class SupervisorsInfoRequest {

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
