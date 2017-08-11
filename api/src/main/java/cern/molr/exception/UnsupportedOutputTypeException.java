/*
 * © Copyright 2017 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.exception;

import cern.molr.mission.Mission;

/**
 * Thrown when the client doesn't know how to deserialize the {@link Mission} output type
 * @author nachivpn
 */
public class UnsupportedOutputTypeException extends Exception {

    private static final long serialVersionUID = -4554885354533951444L;

    public UnsupportedOutputTypeException(String message) {
        super(message);
    }

    public UnsupportedOutputTypeException(Throwable cause) {
        super(cause);
    }

    public UnsupportedOutputTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
