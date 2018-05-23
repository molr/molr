package cern.molr.mole.spawner.debug;

import cern.molr.mission.Mission;
import cern.molr.mole.supervisor.MoleExecutionController;
import cern.molr.mole.supervisor.MoleSession;

/**
 * Session for debugging a mission
 * @author yassine-kr
 */
public class DebugSession implements MoleSession {

    private final Mission mission;
    private DebugController controller;

    public DebugSession(Mission mission, DebugController controller) {
        this.mission = mission;
        this.controller = controller;
    }

    @Override
    public Mission getMission() {
        return mission;
    }

    @Override
    public MoleExecutionController getController() {
        return controller;
    }
}
