/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mole.supervisor;

import java.time.ZonedDateTime;

import cern.molr.mission.Mission;
import cern.molr.mission.controller.custom.StepResult;
import cern.molr.type.either.Either;

/**
 * A generic {@link StepSession}, encapsulates the information of a currently stepping {@link Mission}
 * TODO verify whether this class is relevant
 * @author tiagomr
 * @author nachivpn
 * @author yassine
 */
public interface StepSession{

    JdiController getController();

    /**
     * @return a {@link ZonedDateTime} to timestamp the creation of the {@link StepSession}
     */
    ZonedDateTime getTimeStamp();
    
    <O> Either<StepResult,O> getResult();
    
}
