package cern.molr.inspector.remote;

import cern.molr.inspector.entry.EntryListener;

/**
 * A list of available methods in {@link EntryListener}s.
 */
public enum EntryListenerMethod {
    ON_LOCATION_CHANGE, ON_INSPECTION_END, ON_VM_DEATH;
}
