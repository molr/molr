/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.mission;


import cern.molr.commons.exception.IncompatibleMissionException;
import cern.molr.commons.exception.MissionExecutionException;

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
     * @param missionName the mission name
     * @throws IncompatibleMissionException thrown when the mission is incompatible with the mole
     */
    void verify(String missionName) throws IncompatibleMissionException;

    /**
     * Method which runs a mission
     * @param mission the mission to run
     * @param args the execution arguments
     * @return the output returned by the mission execution
     * @throws MissionExecutionException a wrapper of an exception thrown during the mission execution
     */
    O run(Mission mission, I args) throws MissionExecutionException;

}
