package cern.molr.mole.supervisor;

import cern.molr.mission.Mission;

/**
 * Session corresponding to the execution of a mission by a supervisor
 * @author yassine-kr
 */
public interface MissionSession {

    Mission getMission();

    MoleExecutionController getController();
}
