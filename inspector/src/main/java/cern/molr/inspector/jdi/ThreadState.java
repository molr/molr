/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.jdi;

import com.sun.jdi.Location;

/**
 * An immutable state for a {@link com.sun.jdi.ThreadReference}.
 */
public class ThreadState {


    private final LocationRange inspectableRange;
    private final Location currentLocation;

    /**
     * Creates a state containing the range of the method currently being executed and the current location
     * of the execution.
     *
     * @param inspectableRange The range being inspected.
     * @param currentLocation  The current location of the execution.
     */
    public ThreadState(LocationRange inspectableRange, Location currentLocation) {
        this.inspectableRange = inspectableRange;
        this.currentLocation = currentLocation;
    }

    /**
     * @return The current location of the execution in this state.
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }

    /**
     * @return The starting position for the task in this state (see {@link cern.molr.inspector.entry.EntryState}).
     */
    public Location getStartLocation() {
        return inspectableRange.getStart();
    }

    /**
     * @return The ending position for the task in this state (see {@link cern.molr.inspector.entry.EntryState}).
     */
    public Location getEndLocation() {
        return inspectableRange.getEnd();
    }

    /**
     * Sets the execution location of the thread state and return a new state with the new location.
     *
     * @param location The updated location of the execution.
     * @return A new {@link ThreadState} with the updated execution location.
     */
    public ThreadState setLocation(Location location) {
        return new ThreadState(inspectableRange, location);
    }

}
