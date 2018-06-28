package cern.molr.supervisor.impl.session.runner;

import cern.molr.commons.api.exception.CommandNotAcceptedException;
import cern.molr.commons.api.mission.StateManagerListener;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.commons.api.mission.StateManager;
import cern.molr.commons.events.MissionControlEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * An implementation of the {@link StateManager} used by the MoleRunner
 * It has two states so far, MISSION STARTED, and MISSION NOT YET STARTED
 *
 * @author yassine-kr
 */
public class MoleRunnerStateManager implements StateManager {

    private HashSet<StateManagerListener> listeners = new HashSet<>();
    private boolean missionStarted = false;

    @Override
    public String getStatus() {
        if (missionStarted) {
            return "MISSION STARTED";
        } else {
            return "NOT YET STARTED";
        }
    }

    @Override
    public List<MissionCommand> getPossibleCommands() {
        List<MissionCommand> possibles = new ArrayList<>();
        possibles.add(new MissionControlCommand(MissionControlCommand.Command.TERMINATE));
        if (!missionStarted) {
            possibles.add(new MissionControlCommand(MissionControlCommand.Command.START));
        }
        return possibles;
    }

    @Override
    public void acceptCommand(MissionCommand command) throws CommandNotAcceptedException {
        if (command instanceof MissionControlCommand) {
            MissionControlCommand c = (MissionControlCommand) command;
            switch (c.getCommand()) {
                case START:
                    if (missionStarted) {
                        throw new CommandNotAcceptedException("Command not accepted by the MoleRunner: " +
                                "the mission is already started");
                    }
            }
        }
    }

    @Override
    public void changeState(MissionEvent event) {
        if (event instanceof MissionControlEvent) {
            MissionControlEvent e = (MissionControlEvent) event;
            switch (e.getEvent()) {
                case MISSION_STARTED:
                    missionStarted = true;
                    notifyListeners();
                    break;
            }
        }

    }

    /**
     * Add a listener and notifies it to take into account the current state
     * @param listener the listener to add
     */
    public void addListener(StateManagerListener listener) {
        listeners.add(listener);
        listener.onStateChanged();
    }

    private void notifyListeners() {
        listeners.forEach(StateManagerListener::onStateChanged);
    }
}
