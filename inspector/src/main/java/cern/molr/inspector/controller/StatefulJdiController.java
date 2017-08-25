package cern.molr.inspector.controller;

import cern.molr.inspector.entry.EntryState;

import java.util.Optional;

/**
 * Stateful extension of the {@link JdiController} interface
 * @author mgalilee
 */
public interface StatefulJdiController extends JdiController {
    boolean isDead();

    Optional<EntryState> getLastKnownState();

    void addObserver(JdiStateObserver jdiStateObserver);

    void removeObserver(JdiStateObserver jdiStateObserver);

    interface JdiStateObserver {
        void death();
        void entryStateChanged();
    }
}
