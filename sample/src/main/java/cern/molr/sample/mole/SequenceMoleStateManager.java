package cern.molr.sample.mole;

import cern.molr.commons.api.exception.CommandNotAcceptedException;
import cern.molr.commons.api.mission.StateManager;
import cern.molr.commons.api.mission.StateManagerListener;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.sample.commands.SequenceCommand;
import cern.molr.sample.events.SequenceMissionEvent;
import cern.molr.sample.states.SequenceMissionState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * An implementation of the {@link StateManager} used by the {@link SequenceMole} to manage its state
 * It has three states; WAITING for a task, TASK_RUNNING a task, TASKS_FINISHED all tasks
 *
 * @author yassine-kr
 */
public class SequenceMoleStateManager implements StateManager {

    private final int numTasks;
    private HashSet<StateManagerListener> listeners = new HashSet<>();
    private int currentTask = 0;
    private SequenceMissionState.State state = SequenceMissionState.State.WAITING;

    public SequenceMoleStateManager(int numTasks) {
        this.numTasks = numTasks;
    }

    public SequenceMissionState getSequenceMoleState() {
        int taskNumber = state == SequenceMissionState.State.TASKS_FINISHED ? currentTask : -1;
        return new SequenceMissionState(getStatus(), getPossibleCommands(), taskNumber, state);
    }

    @Override
    public String getStatus() {
        switch (state) {
            case TASK_RUNNING:
                return "TASK_RUNNING TASK " + currentTask;
            case WAITING:
                return "WAITING NEXT TASK " + currentTask;
            case TASKS_FINISHED:
                return "ALL TASKS TASKS_FINISHED";
        }
        return "UNKNOWN STATE";
    }

    @Override
    public List<MissionCommand> getPossibleCommands() {
        List<MissionCommand> possibles = new ArrayList<>();
        if (state.equals(SequenceMissionState.State.WAITING)) {
            possibles.add(new SequenceCommand(SequenceCommand.Command.STEP));
            possibles.add(new SequenceCommand(SequenceCommand.Command.SKIP));
            possibles.add(new SequenceCommand(SequenceCommand.Command.FINISH));
        }
        return possibles;
    }

    @Override
    public void acceptCommand(MissionCommand command) throws CommandNotAcceptedException {
        if (!(command instanceof SequenceCommand)) {
            throw new CommandNotAcceptedException("Command not accepted by the Mole; it is not a known a command by " +
                    "the sequence mole");
        }
        if (state.equals(SequenceMissionState.State.TASK_RUNNING) || state.equals(SequenceMissionState.State.TASKS_FINISHED)) {
            throw new CommandNotAcceptedException("Command not accepted by the Mole; the mission is running or " +
                    "finished");
        }
    }

    @Override
    public void changeState(MissionEvent event) {
        if (event instanceof SequenceMissionEvent) {
            SequenceMissionEvent e = (SequenceMissionEvent) event;
            switch (e.getEvent()) {
                case TASK_STARTED:
                    state = SequenceMissionState.State.TASK_RUNNING;
                    notifyListeners();
                    break;
                case TASK_ERROR:
                case TASK_FINISHED:
                case TASK_SKIPPED:
                    if (e.getTaskNumber() == numTasks - 1) {
                        state = SequenceMissionState.State.TASKS_FINISHED;
                    } else {
                        state = SequenceMissionState.State.WAITING;
                        currentTask = e.getTaskNumber() + 1;
                    }
                    notifyListeners();
                    break;
            }
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

}
