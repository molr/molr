/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.service.custom;

import java.util.concurrent.CompletableFuture;

import cern.molr.exception.custom.UnsupportedOutputTypeException;
import cern.molr.mission.controller.custom.CustomRunMissionController;
import cern.molr.mission.controller.custom.CustomStepMissionController;
import cern.molr.mission.service.MissionExecutionService;


/**
 * {@link CustomMissionExecutionService} is a custom service to run/step a mission. It should be implemented as a
 * layer upon the default {@link MissionExecutionService}
 * 
 * @author nachivpn
 * @author yassine-kr
 */
public interface CustomMissionExecutionService {

    <I,O> CompletableFuture<CustomRunMissionController<O>> runToCompletion(String missionDefnClassName, I args,
                                                                            Class<I> cI, Class<O> cO) throws
            UnsupportedOutputTypeException;
    
    <I,O> CompletableFuture<CustomStepMissionController<O>> step(String missionDefnClassName, I args);
    
}
