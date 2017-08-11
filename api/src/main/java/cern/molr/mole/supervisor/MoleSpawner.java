/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mole.supervisor;

import cern.molr.mission.Mission;

/**
 * The {@link MoleSpawner} runs a given mission by instantiating (or spawning) a mole and asking it to run the mission
 * 
 * @author nachivpn 
 * @param <T> - return type of the spawn action (it could be a session)
 */
public interface MoleSpawner<I,O,T> {

    T spawnMoleRunner(Mission m, I args) throws Exception;

}
