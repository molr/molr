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
     * Method which verifies if a class type is compatible with the mole
     * @param classType the mission class type
     * @throws IncompatibleMissionException thrown when the mission class type is incompatible with the mole
     */
    void verify(Class<?> classType) throws IncompatibleMissionException;

    /**
     * Method which runs a mission
     * @param mission the mission to run
     * @param args the execution arguments
     * @return the output returned by the misison execution
     * @throws MissionExecutionException a wrapper of an exception thrown during the mission execution
     */
    O run(Mission mission, I args) throws MissionExecutionException;

}
