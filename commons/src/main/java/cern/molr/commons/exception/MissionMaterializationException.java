/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.commons.exception;

import cern.molr.commons.mission.Mission;
import cern.molr.commons.mission.MissionMaterializer;

/**
 * Exception to be used whenever its not possible to
 * {@link MissionMaterializer#materialize(Class)} a {@link Mission}
 *
 * @author timartin
 */
public class MissionMaterializationException extends Exception {

    private static final long serialVersionUID = 1L;

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
