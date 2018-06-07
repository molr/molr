package cern.molr.supervisor.api.session.runner;

import cern.molr.commons.exception.CommandNotAcceptedException;
import cern.molr.commons.request.MissionCommand;

/**
 * Represents the MoleRunner state.
 *
 * @author yassine-kr
 */
public interface MoleRunnerState {
    /**
     * Method which verify whether a command is accepted by the current state
     *
     * @param command
     *
     * @throws Exception if the command is not accepted
     */
    void acceptCommand(MissionCommand command) throws CommandNotAcceptedException;

    /**
     * Change the current state according to the last accepted command
     */
    void changeState();


}
