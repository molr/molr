package cern.molr.mole.supervisor;

import cern.molr.mission.Mission;

/**
 * Session corresponding to execution of a mission by a mole
 * @author yassine
 */
public interface MoleSession {

    Mission getMission();

    MoleExecutionController getController();
}
