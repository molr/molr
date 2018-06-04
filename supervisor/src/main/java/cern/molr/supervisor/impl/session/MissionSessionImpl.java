package cern.molr.supervisor.impl.session;

import cern.molr.commons.mission.Mission;
import cern.molr.supervisor.api.session.MissionSession;
import cern.molr.supervisor.api.session.MoleController;

/**
 * Session for running a mission
 *
 * @author yassine-kr
 */
public class MissionSessionImpl implements MissionSession {

    private final Mission mission;
    private ControllerImpl controller;

    public MissionSessionImpl(Mission mission, ControllerImpl controller) {
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
