package cern.molr.server;

import java.util.ArrayList;
import java.util.List;

public class RegisteredMissions {
    private List<String> missions;

    public RegisteredMissions(List<String> missions) {
        this.missions = new ArrayList<>(missions);
    }

    public List<String> getMissions() {
        return new ArrayList<>(missions);
    }
}