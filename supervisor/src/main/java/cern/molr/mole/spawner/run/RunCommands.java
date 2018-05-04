package cern.molr.mole.spawner.run;

import cern.molr.mole.supervisor.MoleExecutionCommand;

/**
 * Commands used to run missions
 * @author yassine
 */
public abstract class RunCommands {
    public static class Start implements MoleExecutionCommand {

        private String missionId;

        public Start() {
        }

        public Start(String missionId) {
            this.missionId = missionId;
        }

        @Override
        public String getMissionId() {
            return missionId;
        }

        @Override
        public void setMissionId(String missionId) {
            this.missionId = missionId;
        }
    }
    public static class Terminate implements MoleExecutionCommand {

        private String missionId;

        public Terminate() {
        }

        public Terminate(String missionId) {
            this.missionId = missionId;
        }

        @Override
        public String getMissionId() {
            return missionId;
        }

        @Override
        public void setMissionId(String missionId) {
            this.missionId = missionId;
        }
    }
}
