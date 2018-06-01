/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.exception;

/**
 * Exception to be used whenever its not possible to
 * {@link cern.molr.mission.MissionMaterializer#materialize(Class)} a {@link cern.molr.mission.Mission}
 *
 * @author timartin
 */
public class MissionMaterializationException extends Exception {

    private static final long serialVersionUID = -2067922831917828440L;

    public MissionMaterializationException(String message) {
        super(message);
    }

    public MissionMaterializationException(Throwable cause) {
        super(cause);
    }

    public MissionMaterializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
