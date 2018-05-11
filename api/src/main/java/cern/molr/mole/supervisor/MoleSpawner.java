/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mole.supervisor;

import cern.molr.mission.Mission;

/**
 * The {@link MoleSpawner} spawns a mission for execution
 * 
 * @author nachivpn
 * @author yassine
 * @param <T> - returns type of the spawn action (it could be a JVM session)
 */
public interface MoleSpawner<I,T> {

    T spawnMoleRunner(Mission m, I args) throws Exception;

}
