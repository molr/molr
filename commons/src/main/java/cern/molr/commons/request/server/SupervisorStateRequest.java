/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.request;

/**
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

    public void setFilter(String filter) {
        this.filter = filter;
    }
}
