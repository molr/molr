package cern.molr.test.supervisor.spawner;

import cern.molr.commons.commands.Start;
import cern.molr.commons.commands.Terminate;
import cern.molr.commons.events.MissionException;
import cern.molr.commons.exception.IncompatibleMissionException;
import cern.molr.commons.mission.Mission;
import cern.molr.commons.mission.MissionImpl;
import cern.molr.commons.response.MissionEvent;
import cern.molr.sample.mole.RunnableMole;
import cern.molr.supervisor.api.session.MissionSession;
import cern.molr.supervisor.api.session.MoleController;
import cern.molr.supervisor.impl.spawner.JVMSpawner;
import cern.molr.test.MissionTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * class for testing types returned by the MoleRunner
 *
 * @author yassine-kr
 */
public class TypesTest {


    @Test
    public void IncompatibleMissionTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(3);

        JVMSpawner<Integer> spawner = new JVMSpawner<>();
        Mission mission = new MissionImpl(RunnableMole.class.getName(), MissionTest.class.getName());
        MissionSession session = spawner.spawnMoleRunner(mission, 100);
        MoleController controller = session.getController();

        List<MissionEvent> events = new ArrayList<>();

        controller.addMoleExecutionListener(event -> {
            System.out.println(event);
            events.add(event);
            signal.countDown();

        });
        controller.sendCommand(new Start());
        controller.sendCommand(new Terminate());

        signal.await();

        Assert.assertEquals(MissionException.class, events.get(1).getClass());
        Assert.assertEquals(IncompatibleMissionException.class,
                ((MissionException) events.get(1)).getThrowable().getClass());
        Assert.assertEquals("Mission must implement Runnable interface",
                ((MissionException) events.get(1)).getThrowable().getMessage());
    }


}