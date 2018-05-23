package cern.molr.mole.spawner.run;

import cern.molr.mole.supervisor.MoleExecutionCommand;

/**
 * Commands used to run missions
 * @author yassine
 */
public abstract class RunCommands {
    public static class Start implements MoleExecutionCommand {

        public Start() {
        }

    }
    public static class Terminate implements MoleExecutionCommand {

        public Terminate() {
        }

    }
}
