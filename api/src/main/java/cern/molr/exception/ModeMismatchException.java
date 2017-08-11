/*
 * © Copyright 2017 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.exception;

/**
 * Exception thrown when a run or step action is attempted when not in run or step mode respectively
 * @author nachivpn
 */
public class ModeMismatchException extends Exception {

    private static final long serialVersionUID = 539943784045330733L;

    public ModeMismatchException(String message) {
        super(message);
    }

    public ModeMismatchException(Throwable cause) {
        super(cause);
    }

    public ModeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
