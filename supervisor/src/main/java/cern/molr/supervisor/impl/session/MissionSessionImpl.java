package cern.molr.mole.spawner.run;

import cern.molr.api.session.MissionSession;
import cern.molr.api.session.MoleController;
import cern.molr.commons.mission.Mission;

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
    public MoleController getController() {
        return controller;
    }
}
