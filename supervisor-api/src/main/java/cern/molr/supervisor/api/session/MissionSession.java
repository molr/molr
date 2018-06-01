package cern.molr.supervisor.api.session;

import cern.molr.commons.mission.Mission;

/**
 * Session corresponding to the execution of a mission by a supervisor
 * @author yassine-kr
 */
public interface MissionSession {

    Mission getMission();

    MoleController getController();
}
