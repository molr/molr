/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mole;


import cern.molr.exception.IncompatibleMissionException;
import cern.molr.exception.MissionExecutionException;
import cern.molr.mission.Mission;

/**
 * A {@link Mole} executes a given mission. Only a {@link Mole} knows how to control a mission.
 * 
 * @author nachivpn
 * @author yassine
 * @param <I>
 * @param <O>
 */
public interface Mole<I, O> {

    /**
     * Method which verify if a class type is compatible with the mole
     * @param classType
     * @throws IncompatibleMissionException
     */
    void verify(Class<?> classType) throws IncompatibleMissionException;
    
    O run(Mission mission, I args) throws MissionExecutionException;
    
}
