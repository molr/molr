/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.step;

import java.util.concurrent.CompletableFuture;

import cern.molr.type.Ack;
import cern.molr.type.either.Either;

public interface StepMissionController<T> {
    
    CompletableFuture<Ack> cancel();

    CompletableFuture<T> runToCompletion();

    CompletableFuture<Either<StepResult, T>> step();
}
