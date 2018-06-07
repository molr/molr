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
        return numMissions < maxMissions;
    }

    public int getNumMissions() {
        return numMissions;
    }

    public int getMaxMissions() {
        return maxMissions;
    }

}
