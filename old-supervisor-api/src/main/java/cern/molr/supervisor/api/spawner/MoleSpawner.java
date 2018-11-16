/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.api.spawner;


import cern.molr.commons.api.mission.Mission;

/**
 * The {@link MoleSpawner} spawns a mission for execution
 *
 * @param <T> - returns type of the spawn action (it could be a JVM session)
 *
 * @author nachivpn
 * @author yassine-kr
 */
public interface MoleSpawner<I, T> {

    T spawnMoleRunner(Mission m, I missionArguments) throws Exception;

}
