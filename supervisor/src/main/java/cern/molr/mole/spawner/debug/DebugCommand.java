package cern.molr.mole.spawner.debug;

import cern.molr.mole.supervisor.MoleExecutionCommand;

/**
 * Command used to debug missions
 * @author yassine
 */
public enum DebugCommand implements MoleExecutionCommand{
    START,TERMINATE,STEP,PAUSE,RESUME,CANCEL;

    private String missionId;

    @Override
    public String getMissionId() {
        return missionId;
    }

    @Override
    public void setMissionId(String missionId) {
        this.missionId=missionId;
    }
}
