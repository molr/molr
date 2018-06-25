package cern.molr.supervisor.impl.session.runner;

import cern.molr.commons.api.exception.CommandNotAcceptedException;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.supervisor.api.session.runner.MoleRunnerState;

/**
 * An implementation of {@link MoleRunnerState}
 *
 * @author yassine-kr
 */
public class MoleRunnerStateImpl implements MoleRunnerState {

    private boolean missionStarted = false;

    @Override
    public void acceptCommand(MissionCommand command) throws CommandNotAcceptedException {
        if (command instanceof MissionControlCommand && ((MissionControlCommand) command)
                .getCommand().equals(MissionControlCommand.Command.START) && missionStarted) {
            throw new CommandNotAcceptedException("Command not accepted by the MoleRunner: the mission is already " +
                    "started");
        }
    }

    @Override
    public void changeState() {
        missionStarted = true;
    }
}
