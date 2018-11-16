package cern.molr.commons.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * A supervisor state
 *
 * @author yassine-kr
 */
public final class SupervisorState {
    private int numMissions;
    private int maxMissions;

    public SupervisorState(@JsonProperty("numMissions") int numMissions, @JsonProperty("maxMission") int maxMissions) {
        this.numMissions = numMissions;
        this.maxMissions = maxMissions;
    }

    @JsonProperty(access = READ_ONLY)
    public boolean isAvailable() {
        return numMissions < maxMissions;
    }

    public int getNumMissions() {
        return numMissions;
    }

    public int getMaxMissions() {
        return maxMissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SupervisorState state = (SupervisorState) o;
        return numMissions == state.numMissions && maxMissions == state.maxMissions;

    }

    @Override
    public int hashCode() {
        int result = numMissions;
        result = 31 * result + maxMissions;
        return result;
    }

    @Override
    public String toString() {
        return "numMissions: " + numMissions + " maxMissions: " + maxMissions;
    }

}
