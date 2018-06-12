package cern.molr.commons.api.request.supervisor;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A request sent by a supervisor to register itself in MolR Server
 *
 * @author yassine-kr
 */
public final class SupervisorRegisterRequest {

    private final String host;
    private final int port;
    private final List<String> acceptedMissions;

    public SupervisorRegisterRequest(@JsonProperty("host") String host, @JsonProperty("port") int port,
                                     @JsonProperty("acceptedMissions") List<String> acceptedMissions) {
        this.host = host;
        this.port = port;
        this.acceptedMissions = acceptedMissions;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public List<String> getAcceptedMissions() {
        return acceptedMissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SupervisorRegisterRequest request = (SupervisorRegisterRequest) o;
        if (host != null ? !host.equals(request.host) : request.host != null) {
            return false;
        }

        if (port != request.port) {
            return false;
        }

        return !(acceptedMissions != null ? !acceptedMissions.equals(request.acceptedMissions) :
                request.acceptedMissions != null);

    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        result = 31 * result + (acceptedMissions != null ? acceptedMissions.hashCode() : 0);
        return result;
    }

}