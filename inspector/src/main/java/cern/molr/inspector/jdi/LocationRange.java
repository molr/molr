/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.jdi;

import java.util.ArrayList;
import java.util.List;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.Method;

/**
 * An immutable range for a piece of code inside a {@link Method} of a {@link Class} measured in
 * {@link com.sun.jdi.Location}s.
 */
public class LocationRange {

    private final Location start;
    private final Location end;

    /**
     * Creates a LocationRange between two locations. We assume the locations are within the same {@link Method} and
     * that the end location comes after start.
     *
     * @param start The starting location of the range.
     * @param end   The ending location of the range.
     */
    private LocationRange(Location start, Location end) {
        this.start = start;
        this.end = end;
    }

    /**
     * @return The start of this range.
     */
    public Location getStart() {
        return start;
    }

    /**
     * @return The end of this range.
     */
    public Location getEnd() {
        return end;
    }

    /**
     * Examines whether the given location is within this range (inclusive). The check also verifies that the given
     * location is in the same class and method as this range.
     *
     * @param location The location to test.
     * @return True if the given location is within the same method as the range and that the following condition holds
     * (in terms of line numbers): <code>start <= location <= end</code>.
     */
    public boolean isWithin(Location location) {
        return location.method().equals(start.method()) && location.lineNumber() >= start.lineNumber()
                && location.lineNumber() <= end.lineNumber();
    }

    /**
     * Creates a location range from the given method, using its start and end points as the range.
     *
     * @param method The method to use.
     * @return A location range spanning from the beginning to the end of the method.
     * @throws AbsentInformationException If the method does not have information about the lines.
     */
    public static LocationRange ofMethod(Method method) throws AbsentInformationException {
        final List<Location> lines = new ArrayList<>(method.allLineLocations());
        if (lines.size() < 1) {
            throw new IllegalArgumentException("Cannot create range for method with 0 lines");
        }

        lines.sort((location1, location2) -> Integer.compare(location1.lineNumber(), location2.lineNumber()));
        return new LocationRange(lines.get(0), lines.get(lines.size() - 1));
    }
}
