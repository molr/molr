package cern.molr.mole.spawner.run.jvm;

import cern.molr.commons.commands.Start;
import cern.molr.commons.exception.CommandNotAcceptedException;
import cern.molr.mole.supervisor.MoleRunnerState;
import cern.molr.commons.request.MissionCommand;

/**
 * An implementation of {@link MoleRunnerState}
 * @author yassine-kr
 */
public class MoleRunnerStateImpl implements MoleRunnerState {

    private boolean missionStarted=false;

    @Override
    public void acceptCommand(MissionCommand command) throws CommandNotAcceptedException {
        if (command instanceof Start && missionStarted)
            throw new CommandNotAcceptedException("Command not accepted by the JVM: the mission is already started");
    }

    @Override
    public void changeState() {
        missionStarted=true;
    }
}
