package cern.molr.sample.mission;

import cern.molr.commons.api.mission.RunWithMole;
import cern.molr.sample.mole.RunnableMole;

/**
 * A testing mission which is not accepted by the impl
 *
 * @author yassine-kr
 */
@RunWithMole(RunnableMole.class)
public class NotAcceptedMission implements Runnable {

    @Override
    public void run() {

    }
}
