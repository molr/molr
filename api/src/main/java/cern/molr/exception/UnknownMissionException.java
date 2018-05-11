/*
 * © Copyright 2017 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.exception;

import cern.molr.mission.Mission;

/**
 * Thrown when execution of an unknown {@link Mission} is requested
 * It is thrown in two cases: when the MolR server does not have the mission in its registry or the request concerns a mission which does not belong to instantiated missions
 * TODO separate these two cases into two different exceptions
 * @author nachivpn
 * @author yassine
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
