/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.service;

import cern.molr.mission.controller.ClientMissionController;
import reactor.core.publisher.Mono;


/**
 * A service used by client to control a remote mission execution on a supervisor
 * @author yassine-kr
 */
public interface MissionExecutionService {

    <I> Mono<ClientMissionController> instantiate(String missionDefnClassName, I args);
    
}
