/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.service;

import cern.molr.exception.UnsupportedOutputTypeException;
import cern.molr.mission.run.RunMissionController;
import cern.molr.mission.run.RunMissionControllerNew;
import cern.molr.mission.step.StepMissionController;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;


/**
 * A service used by client to control a remote mission execution on a supervisor
 * TODO remove "New" from class name
 * @author yassine
 */
public interface MissionExecutionServiceNew {

    <I> Mono<RunMissionControllerNew> instantiate(String missionDefnClassName, I args);
    
}
