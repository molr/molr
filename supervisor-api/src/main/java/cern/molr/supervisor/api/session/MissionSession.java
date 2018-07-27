package cern.molr.supervisor.api.session;

import cern.molr.commons.api.mission.Mission;

/**
 * Session corresponding to a mission execution
 *
 * @author yassine-kr
 */
public interface MissionSession {

    Mission getMission();

    MoleController getController();
}
