package cern.molr.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing list of missions registered in MolR server registry.
 *
 * @author asvec, astanisz
 */
public class RegisteredMissions {
    private List<String> missions;

    /**
     * Instantiates {@link RegisteredMissions}.
     *
     * @param missions canonical paths of mission classes to be registered in MolR server registry
     */
    public RegisteredMissions(List<String> missions) {
        this.missions = new ArrayList<>(missions);
    }

    public List<String> getMissions() {
        return new ArrayList<>(missions);
    }
}