package cern.molr.inspector.controller;

import cern.molr.inspector.entry.EntryListener;
import cern.molr.inspector.entry.EntryState;
import cern.molr.inspector.remote.EntryListenerReader;
import cern.molr.inspector.remote.JdiControllerWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

/**
 * Default implementation of a {@link JdiController}, which holds the current debugging state as a {@link StatefulJdiController}
 *
 * @author mgalilee
 */
public class StatefulJdiControllerImpl extends JdiControllerWriter implements StatefulJdiController, EntryListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatefulJdiControllerImpl.class);

    private boolean dead = false;
    private Optional<EntryState> lastKnownState = Optional.empty();
    //What is this guy used for?
    private final EntryListenerReader entryListenerReader;
    private final Set<JdiStateObserver> observers = new HashSet<>();

    /**
     * Creates a new {@link StatefulJdiControllerImpl}
     * @param process the Process{@link Process} to read the state from and write the commands to
     */
    public StatefulJdiControllerImpl(Process process) {
        super(new PrintWriter(process.getOutputStream()));
        entryListenerReader = new EntryListenerReader(new BufferedReader(new InputStreamReader(process.getInputStream())),
                this);
        entryListenerReader.setOnReadingAttempt(() -> {
            if (!process.isAlive()) {
                LOGGER.error("Spawned debug process is dead!");
                entryListenerReader.close();
                die();
            }
        });
    }

    @Override
    public void onLocationChange(EntryState state) {
        lastKnownState = Optional.of(state);
        entryStateChanged();
    }

    @Override
    public void onInspectionEnd(EntryState state) {
        // not used
    }

    @Override
    public void onVmDeath() {
        die();
    }

    @Override
    public void stepForward() {
        super.stepForward();
        setUnknownEntryState();
    }

    @Override
    public void resume() {
        super.resume();
        setUnknownEntryState();
    }

    @Override
    public void terminate() {
        super.terminate();
        setUnknownEntryState();
    }

    @Override
    public boolean isDead() {
        return dead;
    }

    @Override
    public Optional<EntryState> getLastKnownState() {
        return lastKnownState;
    }

    private void setUnknownEntryState() {
        if(lastKnownState.isPresent()) {
            lastKnownState = Optional.empty();
            entryStateChanged();
        }
    }

    private void die() {
        setUnknownEntryState();
        dead = true;
        observers.forEach(JdiStateObserver::death);
    }

    private void entryStateChanged() {
        observers.forEach(JdiStateObserver::entryStateChanged);
    }

    public void addObserver(JdiStateObserver jdiStateObserver) {
        observers.add(jdiStateObserver);
    }

    public void removeObserver(JdiStateObserver jdiStateObserver) {
        observers.remove(jdiStateObserver);
    }
}