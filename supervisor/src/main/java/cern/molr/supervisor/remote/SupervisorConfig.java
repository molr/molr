package cern.molr.supervisor.remote;

/**
 * A supervisor configuration which contains properties of the launched supervisor
 * @author yassine-kr
 */
public class SupervisorConfig {
    /**
     * Maximum number of parallel missions
     */
    private int maxMissions;

    private String[] acceptedMissions;

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
}
