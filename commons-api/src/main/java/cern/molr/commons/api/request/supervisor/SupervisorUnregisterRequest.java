package cern.molr.commons.api.request.supervisor;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A request sent by a supervisor to unregister itself from MolR server
 *
 * @author yassine-kr
 */
public final class SupervisorUnregisterRequest {

    private final String supervisorId;

    public SupervisorUnregisterRequest(@JsonProperty("supervisorId") String supervisorId) {
        this.supervisorId = supervisorId;
    }

    public String getSupervisorId() {
        return supervisorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SupervisorUnregisterRequest request = (SupervisorUnregisterRequest) o;

        return !(supervisorId != null ? !supervisorId.equals(request.supervisorId) : request.supervisorId != null);

    }

    @Override
    public int hashCode() {
        return supervisorId != null ? supervisorId.hashCode() : 0;
    }

}