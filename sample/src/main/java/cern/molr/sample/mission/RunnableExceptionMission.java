/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.sample.mission;

import cern.molr.commons.RunWithMole;
import cern.molr.sample.mole.RunnableMole;

/**
 * A testing mission which throws an exception
 * @author yassine
 */
@RunWithMole(RunnableMole.class)
public class RunnableExceptionMission implements Runnable{

    @Override
    public void run() {
       throw new RuntimeException();
    }

}
