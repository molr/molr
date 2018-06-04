/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.mission;


import cern.molr.commons.exception.IncompatibleMissionException;
import cern.molr.commons.exception.MissionExecutionException;

/**
 * A {@link Mole} executes a given mission. Only a {@link Mole} knows how to run a mission.
 *
 * @param <I>
 * @param <O>
 *
 * @author nachivpn
 * @author yassine-kr
 */
public interface Mole<I, O> {

    /**
     * Method which verify if a class type is compatible with the mole
     *
     * @param classType
     *
     * @throws IncompatibleMissionException
     */
    void verify(Class<?> classType) throws IncompatibleMissionException;

    O run(Mission mission, I args) throws MissionExecutionException;

}
