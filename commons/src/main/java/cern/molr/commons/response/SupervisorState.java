package cern.molr.commons.response;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SupervisorState {
    private int numMissions;
    private int maxMissions;

    public SupervisorState() {
    }

    public SupervisorState(int numMissions, int maxMissions) {
        this.numMissions = numMissions;
        this.maxMissions = maxMissions;
    }

    @JsonIgnore
    public boolean isAvailable() {
        return numMissions<maxMissions;
    }

    public int getNumMissions() {
        return numMissions;
    }

    public void setNumMissions(int numMissions) {
        this.numMissions = numMissions;
    }

    public int getMaxMissions() {
        return maxMissions;
    }

    public void setMaxMissions(int maxMissions) {
        this.maxMissions = maxMissions;
    }
}
