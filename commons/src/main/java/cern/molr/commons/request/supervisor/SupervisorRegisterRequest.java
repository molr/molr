package cern.molr.commons.request.supervisor;

import java.util.ArrayList;
import java.util.List;

/**
 * A request sent by a supervisor to register itself in MolR Server
 *
 * @author yassine
 */
public class SupervisorRegisterRequest {

    private String host;
    private int port;
    private List<String> acceptedMissions;

    public SupervisorRegisterRequest(){}

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

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAcceptedMissions(ArrayList<String> acceptedMissions) {
        this.acceptedMissions = acceptedMissions;
    }
}