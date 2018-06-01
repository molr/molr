package cern.molr.supervisor.impl.session.runner;

import cern.molr.supervisor.api.session.runner.MoleRunnerState;
import cern.molr.commons.commands.Start;
import cern.molr.commons.exception.CommandNotAcceptedException;
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
            throw new CommandNotAcceptedException("Command not accepted by the Mole runner: the mission is already " +
                    "started");
    }

    @Override
    public void changeState() {
        missionStarted=true;
    }
}
