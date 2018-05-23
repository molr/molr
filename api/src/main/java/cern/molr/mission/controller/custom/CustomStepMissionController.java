/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.controller.custom;

import java.util.concurrent.CompletableFuture;

import cern.molr.mission.Mission;
import cern.molr.mission.controller.ClientMissionController;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;

/**
 * A custom controller used for debugging over a running {@link Mission}
 * implementation should be a layer upon {@link ClientMissionController}
 * 
 * @author nachivpn
 * @author yassine-kr
 * @param <T>
 */
public interface CustomStepMissionController<T> {
    
    CompletableFuture<Ack> cancel();

    CompletableFuture<T> runToCompletion();

    CompletableFuture<Either<StepResult, T>> step();
}
