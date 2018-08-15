/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.api;

import org.reactivestreams.Publisher;

import java.util.function.Function;


/**
 * A service used by the client to launch and control a mission execution
 *
 * @author yassine-kr
 */
public interface MissionExecutionService {

    /**
     * A method which instantiates a mission. This method is asynchronous
     *
     * @param missionName      the name of the mission to be instantiated
     * @param missionArguments the mission arguments, can be null if the mission does not need any arguments
     * @param <I>              the arguments type
     *
     * @return a stream of one element which is the mission controller
     */
    <I> Publisher<ClientMissionController> instantiate(String missionName, I missionArguments);


    /**
     * A synchronous version of the {@link MissionExecutionService#instantiate(String, Object)} method
     */
    <I> ClientMissionController instantiateSync(String missionName, I missionArguments) throws MissionExecutionServiceException;

    /**
     * A method which can be used to use a custom controller
     */
    <I, C extends ClientMissionController> Publisher<C> instantiateCustomController(String missionName, I
            missionArguments, Function<ClientControllerData, C> controllerConstructor);

    /**
     * A synchronous version of the {@link MissionExecutionService#instantiateCustomController(String, Object, Function)}
     */
    <I, C extends ClientMissionController> C instantiateCustomControllerSync(String missionName, I
            missionArguments, Function<ClientControllerData, C> controllerConstructor) throws MissionExecutionServiceException;
}
