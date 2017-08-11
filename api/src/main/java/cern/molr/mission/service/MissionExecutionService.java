/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.service;

import java.util.concurrent.CompletableFuture;

import cern.molr.mission.run.RunMissionController;
import cern.molr.mission.step.StepMissionController;


/**
 * {@link MisisonExecutionService} is the client (operator) - server interaction interface to run/step through missions
 * 
 * @author nachivpn
 */
public interface MissionExecutionService {

    <I,O> CompletableFuture<RunMissionController<O>> runToCompletion(String missionDefnClassName, I args, Class<I> cI, Class<O> cO);
    
    <I,O> CompletableFuture<StepMissionController<O>> step(String missionDefnClassName, I args);
    
}
