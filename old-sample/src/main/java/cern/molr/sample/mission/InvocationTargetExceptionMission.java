/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.sample.mission;

import cern.molr.commons.api.mission.RunWithMole;
import cern.molr.sample.mole.RunnableMole;

import java.lang.reflect.InvocationTargetException;

/**
 * A testing mission which throws an exception
 *
 * @author yassine-kr
 */
@RunWithMole(RunnableMole.class)
public class InvocationTargetExceptionMission implements Runnable {

    @Override
    public void run() {
        throw new RuntimeException(new InvocationTargetException(new Exception(), "invocation target exception"));
    }

}
