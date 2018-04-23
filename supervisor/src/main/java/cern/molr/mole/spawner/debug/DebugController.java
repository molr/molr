package cern.molr.mole.spawner.debug;

import cern.molr.inspector.entry.EntryListener;
import cern.molr.inspector.entry.EntryState;
import cern.molr.inspector.remote.EntryListenerReader;
import cern.molr.inspector.remote.JdiControllerCommand;
import cern.molr.mole.supervisor.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import static cern.molr.mole.spawner.debug.DebugCommand.START;

/**
 * A controller which controls debugging of a mission.
 * It communicates with JVM using output and input streams
 *
 * @author yassine
 */
public class DebugController implements MoleExecutionController,MoleExecutionListener {

    private final Set<MoleExecutionListener> listeners=new HashSet<>();
    private final PrintWriter printWriter;
    /**
     * Process corresponding to seperate JVM
     */
    private final Process process;

    /**
     * Listener for entries
      */
    private final EntryListenerReader entryListenerReader;

    public DebugController(Process process) {
        this.process=process;
        printWriter=new PrintWriter(process.getOutputStream());

        entryListenerReader = new EntryListenerReader(new BufferedReader(new InputStreamReader(process.getInputStream())), new EntryListener() {
            @Override
            public void onLocationChange(EntryState state) {
                DebugController.this.onEvent(new DebugEvents.LocationChanged(state.getClassName(),state.getMethodName(),state.getLine()));
            }

            @Override
            public void onInspectionEnd(EntryState state) {
                DebugController.this.onEvent(new DebugEvents.InspectionEnded(state.getClassName(),state.getMethodName(),state.getLine()));
            }

            @Override
            public void onVmDeath() {
                DebugController.this.onEvent(new DebugEvents.VmDeath());
            }
        });

    }

    @Override
    public void addMoleExecutionListener(MoleExecutionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeMoleExecutionListener(MoleExecutionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public MoleExecutionRequestCommandResult start() {
        //printWriter.print();
        printWriter.flush();
        return new RequestCommandResult.RequestCommandResultSuccess();
    }

    @Override
    public MoleExecutionRequestCommandResult sendCommand(MoleExecutionCommand command) {
        DebugCommand c=(DebugCommand)command;
        switch(c){
            case STEP:
                printWriter.print(JdiControllerCommand.STEP_FORWARD.ordinal());
                printWriter.flush();
                break;
            case RESUME:
                printWriter.print(JdiControllerCommand.RESUME.ordinal());
                printWriter.flush();
                break;
        }
        return new RequestCommandResult.RequestCommandResultSuccess();
    }

    @Override
    public MoleExecutionRequestCommandResult terminate() {
        printWriter.print(JdiControllerCommand.TERMINATE.ordinal());
        printWriter.flush();
        return new RequestCommandResult.RequestCommandResultSuccess();
    }

    @Override
    public void onEvent(MoleExecutionEvent event) {
        listeners.forEach((l)->l.onEvent(event));
    }
}
