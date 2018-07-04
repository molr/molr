package cern.molr.supervisor;

import java.time.Duration;

/**
 * A supervisor configuration which contains properties of the launched supervisor
 *
 * @author yassine-kr
 */
public class SupervisorConfig {
    /**
     * Maximum number of parallel missions
     */
    private int maxMissions;

    private String[] acceptedMissions;

    private String molrHost;

    private int molrPort;

    private String supervisorHost;

    private int supervisorPort;

    private Duration heartbeatInterval;

    public Duration getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(Duration heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public String getSupervisorHost() {
        return supervisorHost;
    }

    public void setSupervisorHost(String supervisorHost) {
        this.supervisorHost = supervisorHost;
    }

    public int getSupervisorPort() {
        return supervisorPort;
    }

    public void setSupervisorPort(int supervisorPort) {
        this.supervisorPort = supervisorPort;
    }

    public int getMaxMissions() {
        return maxMissions;
    }

    public void setMaxMissions(int maxMissions) {
        this.maxMissions = maxMissions;
    }

    public String[] getAcceptedMissions() {
        return acceptedMissions;
    }

    public void setAcceptedMissions(String[] acceptedMissions) {
        this.acceptedMissions = acceptedMissions;
    }

    public String getMolrHost() {
        return molrHost;
    }

    public void setMolrHost(String molrHost) {
        this.molrHost = molrHost;
    }

    public int getMolrPort() {
        return molrPort;
    }

    public void setMolrPort(int molrPort) {
        this.molrPort = molrPort;
    }
}
