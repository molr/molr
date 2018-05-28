package cern.molr.mole.spawner.run;

import cern.molr.mission.Mission;
import cern.molr.mole.supervisor.MoleExecutionController;
import cern.molr.mole.supervisor.MissionSession;

/**
 * Session for running a mission
 * @author yassine-kr
 */
public class RunSession implements MissionSession {

    private final Mission mission;
    private RunController controller;

    public RunSession(Mission mission, RunController controller) {
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
