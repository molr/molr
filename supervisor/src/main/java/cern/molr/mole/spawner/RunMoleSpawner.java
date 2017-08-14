/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mole.spawner;

import java.util.concurrent.CompletionException;

import cern.molr.mission.Mission;
import cern.molr.mole.Mole;
import cern.molr.mole.supervisor.MoleSpawner;

/**
 * 
 * Mole spawner impl for run mode
 * @author nachivpn 
 * @param <I>
 * @param <O>
 */
public class RunMoleSpawner<I,O> implements MoleSpawner<I,O,O>{

    @Override
    public O spawnMoleRunner(Mission m, I args) throws Exception{
        try {
            String moleClassName = m.getMoleClassName();
            @SuppressWarnings("unchecked")
            Class<Mole<I, O>> moleClass = (Class<Mole<I, O>>) Class.forName(moleClassName);
            Mole<I,O> mole = moleClass.getConstructor().newInstance();
            return mole.run(m, args);
        } catch (ClassCastException e) {
            throw new CompletionException("BAD argument! Wrong type?",e);
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

}