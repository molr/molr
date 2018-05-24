package cern.molr.mole.supervisor;

import cern.molr.mission.Mission;

/**
 * Session corresponding to the execution of a mission by a mole
 * @author yassine-kr
 */
public interface MoleSession {

    Mission getMission();

    MoleExecutionController getController();
}
