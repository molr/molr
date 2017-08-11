/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.step;

import java.time.ZonedDateTime;

import cern.molr.mole.supervisor.JdiController;
import cern.molr.mole.supervisor.Session;
import cern.molr.type.either.Either;

/**
 * A generic {@link StepSession}, encapsulates the information of a currently stepping {@link Mission}
 *
 * @author tiagomr
 * @author nachivpn
 */
public interface StepSession extends Session{

    JdiController getController();

    /**
     * @return a {@link ZonedDateTime} to timestamp the creation of the {@link StepSession}
     */
    ZonedDateTime getTimeStamp();
    
    <O> Either<StepResult,O> getResult();
    
}
