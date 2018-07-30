package cern.molr.commons.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Information about a supervisor sent to the client to keep track of registered supervisors
 */
public final class SupervisorInfo {

    /**
     * The supervisor id
     */
    final private String id;

    /**
     * The missions being executed on the supervisor
     */
    final private List<String> missions;

    final private SupervisorState state;

    final private Life life;

    public SupervisorInfo(@JsonProperty("id") String id,@JsonProperty("missions") List<String> missions, @JsonProperty
            ("state") SupervisorState state,@JsonProperty("life") Life life) {
        this.id = id;
        this.missions = missions;
        this.state = state;
        this.life = life;
    }

    public String getId() {
        return id;
    }

    public List<String> getMissions() {
        return missions;
    }

    public SupervisorState getState() {
        return state;
    }

    public Life getLife() {
        return life;
    }

    @Override
    public String toString() {
        return id + " " + missions + " " + " " + state + " " + life;
    }

    public enum Life {
        ALIVE, //No problem
        DYING, //Time out reached
        DEAD, //Max time outs reached
        TOMB //Removed from the register
    }

}
