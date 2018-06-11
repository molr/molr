/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.api;

import org.reactivestreams.Publisher;


/**
 * A service used by the client to launch and control a remote mission execution on a supervisor
 *
 * @author yassine-kr
 */
public interface MissionExecutionService {

    /**
     * A method which sends a mission instantiation request to the MolR server
     *
     * @param missionName      the name of the mission to be instantiated
     * @param args             the mission arguments, can be null if the mission does not need any arguments
     * @param <I>              the arguments type
     *
     * @return A stream of one element which is the mission controller
     */
    <I> Publisher<ClientMissionController> instantiate(String missionName, I args);

}
