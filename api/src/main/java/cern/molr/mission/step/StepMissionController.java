/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.step;

import java.util.concurrent.CompletableFuture;

import cern.molr.mission.Mission;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;

/**
 * Controller used to step through a "stepping" {@link Mission} i.e, {@link Mission} running in step mode
 * 
 * @author nachivpn 
 * @param <T>
 */
public interface StepMissionController<T> {
    
    CompletableFuture<Ack> cancel();

    CompletableFuture<T> runToCompletion();

    CompletableFuture<Either<StepResult, T>> step();
}
