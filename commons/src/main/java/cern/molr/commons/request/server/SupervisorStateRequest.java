/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.request.server;

/**
 * A request sent by MolR server to the supervisor to get its state
 *
 * @author yassine-kr
 */
public class SupervisorStateRequest {

    private String filter;

    public SupervisorStateRequest() {
    }

    public SupervisorStateRequest(String filter) {
        this.filter = filter;
    }

    public String getFilter() {
        return filter;
    }

}
