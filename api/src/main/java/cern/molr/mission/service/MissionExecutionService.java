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

    /**
     * A method which send a mission instantiation request to the MolR server
     * @param missionDefnClassName the class name of the mission to be instantiated
     * @param args the mission arguments, can be null if the mission does not need any argument
     * @param <I>
     * @return A future mission controller
     */
    <I> Mono<ClientMissionController> instantiate(String missionDefnClassName, I args);
    
}
