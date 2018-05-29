package cern.molr.mole.spawner;

import cern.molr.commons.MissionImpl;
import cern.molr.exception.IncompatibleMissionException;
import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.spawner.run.RunSpawner;
import cern.molr.mole.supervisor.MoleExecutionController;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MissionSession;
import cern.molr.sample.mole.RunnableMole;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * class for testing types returned by the JVM
 *
 * @author yassine-kr
 */
public class TypesTest {


    @Test
    public void IncompatibleMissionTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(3);

        RunSpawner<Integer> spawner= new RunSpawner<>();
        Mission mission=new MissionImpl(RunnableMole.class.getName(),MissionTest.class.getName());
        MissionSession session=spawner.spawnMoleRunner(mission,100);
        MoleExecutionController controller=session.getController();

        List<MoleExecutionEvent> events=new ArrayList<>();

        controller.addMoleExecutionListener(event -> {
            System.out.println(event);
            events.add(event);
            signal.countDown();

        });
        controller.sendCommand(new RunCommands.Start());
        controller.sendCommand(new RunCommands.Terminate());

        signal.await();

        Assert.assertEquals(RunEvents.MissionException.class,events.get(1).getClass());
        Assert.assertEquals(IncompatibleMissionException.class,
                ((RunEvents.MissionException)events.get(1)).getThrowable().getClass());
        Assert.assertEquals("Mission must implement Runnable interface",
                ((RunEvents.MissionException)events.get(1)).getThrowable().getMessage());
    }


}