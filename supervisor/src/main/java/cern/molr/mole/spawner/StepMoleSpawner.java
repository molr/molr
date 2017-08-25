/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mole.spawner;

import cern.molr.mission.Mission;
import cern.molr.mission.step.StepSession;
import cern.molr.mole.supervisor.MoleSpawner;

/**
 * Mole spawner impl for step mode
 * 
 * @author nachivpn 
 * @param <I>
 * @param <O>
 */
public class StepMoleSpawner<I,O> implements MoleSpawner<I,O,StepSession>{
    @Override
    public StepSession spawnMoleRunner(Mission m, I args) throws Exception{
        throw new RuntimeException("Step mole spawner has not been implemented!");
    }
}