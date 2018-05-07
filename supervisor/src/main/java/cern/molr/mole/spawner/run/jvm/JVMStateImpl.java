package cern.molr.mole.spawner.run.jvm;

import cern.molr.exception.CommandNotAcceptedException;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.supervisor.JVMState;
import cern.molr.mole.supervisor.MoleExecutionCommand;

/**
 * An implementation of {@link JVMState}
 * @author yassine
 */
public class JVMStateImpl implements JVMState {

    private boolean missionStarted=false;

    @Override
    synchronized public void acceptCommand(MoleExecutionCommand command) throws CommandNotAcceptedException {
        if (command instanceof RunCommands.Start && missionStarted)
            throw new CommandNotAcceptedException("Command not accepted by the JVM: the mission is already started");
    }

    @Override
    public void changeState() {
        missionStarted=true;
    }
}
