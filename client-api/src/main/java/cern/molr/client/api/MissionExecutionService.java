/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.api;

import reactor.core.publisher.Mono;


/**
 * A service used by client to control a remote mission execution on a supervisor
 * @author yassine-kr
 */
public interface MissionExecutionService {

    /**
     * A method which send a mission instantiation request to the MolR server
     * @param missionClassName the class name of the mission to be instantiated
     * @param args the mission arguments, can be null if the mission does not need any argument
     * @param <I> the argument type
     * @return A future mission controller
     */
    <I> Mono<ClientMissionController> instantiate(String missionClassName, I args);
    
}
