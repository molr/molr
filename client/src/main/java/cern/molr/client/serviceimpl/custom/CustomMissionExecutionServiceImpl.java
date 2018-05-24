/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.serviceimpl.custom;

import cern.molr.client.serviceimpl.MissionExecutionServiceImpl;
import cern.molr.exception.custom.UnsupportedOutputTypeException;
import cern.molr.mission.controller.custom.CustomRunMissionController;
import cern.molr.mission.service.custom.CustomMissionExecutionService;
import cern.molr.mission.controller.custom.CustomStepMissionController;

import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the custom service, it uses {@link MissionExecutionServiceImpl}
 * 
 * @author nachivpn
 * @author yassine-kr
 */
public class CustomMissionExecutionServiceImpl implements CustomMissionExecutionService {

    @Override
    public <I, O> CompletableFuture<CustomRunMissionController<O>> runToCompletion(String missionDefnClassName, I
            args, Class<I> cI, Class<O> cO) throws UnsupportedOutputTypeException {
        if(!cO.equals(Integer.class) && !cO.equals(String.class) && !cO.equals(Void.class))
            throw new UnsupportedOutputTypeException("Error occured on client: " + cO.getName() + " is not supported" +
                    " yet!");
        //TODO to implement
        return null;
    }

    @Override
    public <I, O> CompletableFuture<CustomStepMissionController<O>> step(String missionDefnClassName, I args) {
        //TODO to implement
        return null;
    }

}
