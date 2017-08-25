/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“.ing this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.entry;

/**
 * Handles callbacks from a JVM running an <i>entry</i>. This class is unique for each entry encountered in the running
 * VM.
 */
public interface EntryListener {

    /**
     * Called by the running JVM instance when an entry is resumed and the location changes to a new line. The VM is
     * paused immediately after an entry has been reached, so the state <i>should</i> be real-time. However,
     * the controller will resume execution upon request, so the state might have changed by the time this method is
     * called.
     *
     * @param state The state of this entry.
     */
    void onLocationChange(EntryState state);

    /**
     * Called by the running JVM instance when an entry reaches the end of its execution. After this call, the entry
     * will be deleted and this listener instance will never be called again.
     *
     * @param state The state of the entry. The position of the entry will point to the last line of the method.
     */
    void onInspectionEnd(EntryState state);

    void onVmDeath();

}
