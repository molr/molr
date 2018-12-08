/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package io.molr.commons.domain;

public enum Result {
    UNDEFINED,
    SUCCESS,
    FAILED;

    public static final Result summaryOf(Iterable<Result> values) {
        for (Result value : values) {
            if (FAILED == value) {
                return FAILED;
            }
        }
        for (Result value : values) {
            if (UNDEFINED == value) {
                return UNDEFINED;
            }
        }
        return SUCCESS;
    }
}
