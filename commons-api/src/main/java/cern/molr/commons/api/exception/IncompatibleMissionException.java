/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.commons.api.exception;

/**
 * Exception to be used whenever a given mission definition class doesn't conform to the expectations of the mole
 *
 * @author nachivpn
 */
public class IncompatibleMissionException extends Exception {

    private static final long serialVersionUID = 1L;

    public IncompatibleMissionException(String message) {
        super(message);
    }

    public IncompatibleMissionException(Throwable cause) {
        super(cause);
    }

    public IncompatibleMissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
