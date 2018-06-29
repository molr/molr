/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.mission;


import cern.molr.commons.api.exception.CommandNotAcceptedException;
import cern.molr.commons.api.exception.IncompatibleMissionException;
import cern.molr.commons.api.exception.MissionExecutionException;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import org.reactivestreams.Publisher;

/**
 * A {@link Mole} executes a given mission. Only a {@link Mole} knows how to run a mission.
 *
 * @param <I> the input type
 * @param <O> the output type
 *
 * @author nachivpn
 * @author yassine-kr
 */
public interface Mole<I, O> {

    /**
     * Method which verifies if a mission is compatible with the mole
     *
     * @param missionName the mission name
     *
     * @throws IncompatibleMissionException thrown when the mission is incompatible with the mole
     */
    void verify(String missionName) throws IncompatibleMissionException;

    /**
     * Method which runs a mission
     *
     * @param mission          the mission to run
     * @param missionArguments the execution arguments
     *
     * @return the output returned by the mission execution
     * @throws MissionExecutionException a wrapper of an exception thrown during the mission execution
     */
    O run(Mission mission, I missionArguments) throws MissionExecutionException;

    /**
     * It should send a command to the mole; the MoleRunner use it to forward commands which are not interpreted
     *
     * @throws CommandNotAcceptedException when the command is not accepted by the mole
     */
    void sendCommand(MissionCommand command) throws CommandNotAcceptedException;

    /**
     * It should return the stream of events triggered by the mole itself during the mission execution
     *
     * @return the events stream
     */
    Publisher<MissionEvent> getEventsPublisher();

    /**
     * It should return the stream of states triggered by the mole itself during the mission execution
     *
     * @return the states stream
     */
    Publisher<MissionState> getStatesPublisher();

}
