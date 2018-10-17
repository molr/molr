/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.domain;

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
        for (RunState value : values) {
            if (UNDEFINED == value) {
                return UNDEFINED;
            }
        }
        return FINISHED;
    }
}
