package cern.molr.commons.api.response;

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

    public SupervisorInfo(String id, List<String> missions, SupervisorState state, Life life) {
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

    public enum Life {
        ALIVE, //No problem
        DYING, //Time out reached
        DEAD, //Max time outs reached
        TOMB //Removed from the register
    }

}
