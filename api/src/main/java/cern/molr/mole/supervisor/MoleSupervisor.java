/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mole.supervisor;

import java.util.concurrent.CompletableFuture;

import cern.molr.mission.Mission;
import cern.molr.mission.step.StepResult;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;
/**
 * The {@link MoleSupervisor} allows interaction with the mole executing a specific mission.
 * This is the entry point for the server to control a mission execution.
 * 
 * @author nachivpn
 */
public interface MoleSupervisor {

    /**
     * Commands the mole supervisor to start a mission 
     * ... and that's it! Result is reported to server when available. No correspondence of result returned by a start
     * @param m
     * @param args
     * @param missionExecutionId
     * @return
     */
    <I,O> CompletableFuture<O> run(Mission m, I args, String missionExecutionId);

    <I,O> CompletableFuture<Either<StepResult, O>> step(Mission m, I args, String missionExecutionId);

    <I,O> CompletableFuture<O> resume(Mission m, I args, String missionExecutionId);
    
    CompletableFuture<Ack> cancel(String missionExecutionId);
    
}
