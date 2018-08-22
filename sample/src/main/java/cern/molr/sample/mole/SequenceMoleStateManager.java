package cern.molr.sample.mole;

import cern.molr.commons.api.exception.CommandNotAcceptedException;
import cern.molr.commons.api.mission.StateManager;
import cern.molr.commons.api.mission.StateManagerListener;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import cern.molr.sample.commands.SequenceCommand;
import cern.molr.sample.events.SequenceMissionEvent;
import cern.molr.sample.states.SequenceMissionState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * An implementation of the {@link StateManager} used by the {@link SequenceMole} to manage its state
 * It has four states; WAITING for a task, TASK_RUNNING, RUNNING_AUTOMATIC, TASKS_FINISHED
 *
 * @author yassine-kr
 */
public class SequenceMoleStateManager implements StateManager {

    private final int numTasks;
    private HashSet<StateManagerListener> listeners = new HashSet<>();
    private int currentTask = 0;
    private SequenceMissionState.State state = SequenceMissionState.State.WAITING;
    private boolean automatic = false;

    public SequenceMoleStateManager(int numTasks) {
        this.numTasks = numTasks;
    }

    @Override
    public MissionState getState() {
        int taskNumber = (state == SequenceMissionState.State.TASKS_FINISHED) ? -1 : currentTask;
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
            case RUNNING_AUTOMATIC:
                return "RUNNING TASK AUTOMATIC " + currentTask;
        }
        return "UNKNOWN STATE";
    }

    @Override
    public List<MissionCommand> getPossibleCommands() {
        List<MissionCommand> possibles = new ArrayList<>();
        if (state.equals(SequenceMissionState.State.WAITING)) {
            possibles.add(new SequenceCommand(SequenceCommand.Command.STEP));
            possibles.add(new SequenceCommand(SequenceCommand.Command.SKIP));
            possibles.add(new SequenceCommand(SequenceCommand.Command.RESUME));
        } else if (state.equals(SequenceMissionState.State.RUNNING_AUTOMATIC)) {
            possibles.add(new SequenceCommand(SequenceCommand.Command.PAUSE));
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
                    "finished, no possibles commands");
        } else if (state.equals(SequenceMissionState.State.RUNNING_AUTOMATIC)) {
            if (!((SequenceCommand) command).getCommand().equals(SequenceCommand.Command.PAUSE)) {
                throw new CommandNotAcceptedException("Command not accepted by the Mole; the only possible command when " +
                        "the mission is running automatically is PAUSE");
            }
        } else if (((SequenceCommand) command).getCommand().equals(SequenceCommand.Command.PAUSE)) {
            throw new CommandNotAcceptedException("Command not accepted by the Mole; PAUSE command is not accepted " +
                    "when the mission is waiting");
        }
    }

    @Override
    public void changeState(MissionEvent event) {
        if (event instanceof SequenceMissionEvent) {
            SequenceMissionEvent e = (SequenceMissionEvent) event;
            switch (e.getEvent()) {
                case RESUMED:
                    automatic = true;
                    break;
                case PAUSED:
                    automatic = false;
                    //The task number is the next task number, we test whether there are more tasks to execute
                    if (e.getTaskNumber() < numTasks) {
                        state = SequenceMissionState.State.WAITING;
                        notifyListeners();
                    }
                    break;
                case TASK_STARTED:
                    if (automatic) {
                        state = SequenceMissionState.State.RUNNING_AUTOMATIC;
                    } else {
                        state = SequenceMissionState.State.TASK_RUNNING;
                    }
                    notifyListeners();
                    break;
                case TASK_ERROR:
                case TASK_FINISHED:
                case TASK_SKIPPED:
                    if (e.getTaskNumber() == numTasks - 1) {
                        state = SequenceMissionState.State.TASKS_FINISHED;
                        notifyListeners();
                    } else {
                        currentTask = e.getTaskNumber() + 1;
                        if (!automatic) {
                            state = SequenceMissionState.State.WAITING;
                            notifyListeners();
                        }
                    }
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
