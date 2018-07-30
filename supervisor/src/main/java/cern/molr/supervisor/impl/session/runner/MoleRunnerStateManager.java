package cern.molr.supervisor.impl.session.runner;

import cern.molr.commons.api.exception.CommandNotAcceptedException;
import cern.molr.commons.api.mission.StateManager;
import cern.molr.commons.api.mission.StateManagerListener;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.commons.events.MissionControlEvent;
import cern.molr.commons.events.MissionExceptionEvent;
import cern.molr.commons.events.MissionFinished;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * An implementation of the {@link StateManager} used by the MoleRunner.
 * It has these states so far, MISSION STARTED, MISSION NOT YET STARTED, MISSION FINISHED, MISSION ERROR, SESSION
 * TERMINATED
 *
 * @author yassine-kr
 */
public class MoleRunnerStateManager implements StateManager {

    private HashSet<StateManagerListener> listeners = new HashSet<>();
    private State state = State.NOT_YET_STARTED;

    @Override
    public String getStatus() {
        switch (state) {
            case NOT_YET_STARTED:
                return "NOT YET STARTED";
            case MISSION_STARTED:
                return "MISSION STARTED";
            case MISSION_FINISHED:
                return "MISSION FINISHED";
            case MISSION_ERROR:
                return "MISSION ERROR";
            case SESSION_TERMINATED:
                return "SESSION TERMINATED";
        }
        return "UNKNOWN STATE";
    }

    @Override
    public List<MissionCommand> getPossibleCommands() {
        List<MissionCommand> possibles = new ArrayList<>();
        if (state.equals(State.NOT_YET_STARTED) || state.equals(State.MISSION_STARTED)) {
            possibles.add(new MissionControlCommand(MissionControlCommand.Command.TERMINATE));
        }
        if (state.equals(State.NOT_YET_STARTED)) {
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
                    if (!state.equals(State.NOT_YET_STARTED)) {
                        throw new CommandNotAcceptedException("Command not accepted by the MoleRunner: " +
                                "the mission is already started");
                    }
                case TERMINATE:
                    if (!(state.equals(State.NOT_YET_STARTED) || state.equals(State.MISSION_STARTED))) {
                        throw new CommandNotAcceptedException("Command not accepted by the MoleRunner: " +
                                "the mission is already terminated");
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
                    state = State.MISSION_STARTED;
                    notifyListeners();
                    break;
                case SESSION_TERMINATED:
                    state = State.SESSION_TERMINATED;
                    notifyListeners();
                    break;
            }
        } else if (event instanceof MissionFinished) {
            state = State.MISSION_FINISHED;
            notifyListeners();
        } else if (event instanceof MissionExceptionEvent) {
            state = State.MISSION_ERROR;
            notifyListeners();
        }

    }

    /**
     * Add a listener and notifies it to take into account the current state
     *
     * @param listener the listener to add
     */
    public void addListener(StateManagerListener listener) {
        listeners.add(listener);
        listener.onStateChanged();
    }

    private void notifyListeners() {
        listeners.forEach(StateManagerListener::onStateChanged);
    }

    private enum State {
        NOT_YET_STARTED,
        MISSION_STARTED,
        MISSION_FINISHED,
        MISSION_ERROR,
        SESSION_TERMINATED
    }
}
