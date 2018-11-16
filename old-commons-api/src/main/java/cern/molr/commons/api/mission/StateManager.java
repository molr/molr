package cern.molr.commons.api.mission;

import cern.molr.commons.api.exception.CommandNotAcceptedException;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;

import java.util.List;

/**
 * It is used by the MoleRunner to manage its own states. It can also be used by a Mole to manage its states.
 *
 * @author yassine-kr
 */
public interface StateManager {

    /**
     * @return the current state
     */
    MissionState getState();

    /**
     * @return a string representing the state status
     */
    String getStatus();

    /**
     * @return the possible commands which are accepted by the state
     */
    List<MissionCommand> getPossibleCommands();

    /**
     * Method which verifies whether a command is accepted by the current state
     *
     * @param command the command to verify
     *
     * @throws CommandNotAcceptedException if the command is not accepted
     */
    void acceptCommand(MissionCommand command) throws CommandNotAcceptedException;

    /**
     * Changes the current state according to an event
     *
     * @param event the triggered event
     */
    void changeState(MissionEvent event);

    void addListener(StateManagerListener listener);
}
