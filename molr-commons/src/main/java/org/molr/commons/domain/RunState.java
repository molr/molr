/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.domain;

import java.util.stream.StreamSupport;

public enum RunState {
    UNDEFINED,
    RUNNING,
    PAUSED,
    FINISHED;

    public static final RunState summaryOf(Iterable<RunState> values) {
        for (RunState value : values) {
            if (RUNNING == value) {
                return RUNNING;
            }
        }
        for (RunState value : values) {
            if (PAUSED == value) {
                return PAUSED;
            }
        }

        if (StreamSupport.stream(values.spliterator(), false).allMatch(FINISHED::equals)) {
            return FINISHED;
        }
        return UNDEFINED;

    }
}
