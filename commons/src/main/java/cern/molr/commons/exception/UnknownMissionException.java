/*
 * © Copyright 2017 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.commons.exception;

import cern.molr.commons.mission.Mission;

/**
 * Thrown when a request concerning an unknown {@link Mission} is received by MolR server or the supervisor
 *
 * @author nachivpn
 * @author yassine-kr
 */
public class UnknownMissionException extends Exception {

    private static final long serialVersionUID = 195586081128114794L;

    public UnknownMissionException(String message) {
        super(message);
    }

    public UnknownMissionException(Throwable cause) {
        super(cause);
    }

    public UnknownMissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
