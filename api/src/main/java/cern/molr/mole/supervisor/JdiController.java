/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mole.supervisor;

import cern.molr.mission.controller.custom.StepResult;
import cern.molr.type.either.Either;

/**
 * Controller used by {@link MoleSupervisor} to control stepping missions
 * TODO verify whether this class is relevant
 * @author nachivpn
 * @author yassine-kr
 */
public interface JdiController {
    
    <T> Either<StepResult,T> stepForward();

    <T> T resume();

    void terminate();
}