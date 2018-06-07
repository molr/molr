package cern.molr.commons.request.supervisor;

import java.util.List;

/**
 * A request sent by a supervisor to register itself in MolR Server
 *
 * @author yassine-kr
 */
public class SupervisorRegisterRequest {

    private String host;
    private int port;
    private List<String> acceptedMissions;

    public SupervisorRegisterRequest() {
    }

    public SupervisorRegisterRequest(String host, int port, List<String> acceptedMissions) {
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

}