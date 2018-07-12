/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.api;

import org.reactivestreams.Publisher;


/**
 * A service used by the client to launch and control a mission execution
 *
 * @author yassine-kr
 */
public interface MissionExecutionService {

    /**
     * A method which instantiates a mission
     *
     * @param missionName      the name of the mission to be instantiated
     * @param missionArguments the mission arguments, can be null if the mission does not need any arguments
     * @param <I>              the arguments type
     *
     * @return a stream of one element which is the mission controller
     */
    <I> Publisher<ClientMissionController> instantiate(String missionName, I missionArguments);

}
