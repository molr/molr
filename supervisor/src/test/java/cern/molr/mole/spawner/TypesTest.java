package cern.molr.mole.spawner;

import cern.molr.commons.MissionImpl;
import cern.molr.commons.response.CommandResponse;
import cern.molr.exception.IncompatibleMissionException;
import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.spawner.run.RunSpawner;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.mole.supervisor.MoleExecutionController;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleSession;
import cern.molr.sample.mole.RunnableMole;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * class for testing types returned by the JVM
 * Each test can fail if the thread finishes before getting all results from supervisor, in that case sleep duration should be increased
 *
 * @author yassine-kr
 */
public class TypesTest {


    @Test
    public void IncompatibleMissionTest() throws Exception {
        RunSpawner<Integer> spawner= new RunSpawner<>();
        Mission mission=new MissionImpl(RunnableMole.class.getName(),MissionTest.class.getName());
        MoleSession session=spawner.spawnMoleRunner(mission,100);
        MoleExecutionController controller=session.getController();

        List<MoleExecutionEvent> events=new ArrayList<>();

        controller.addMoleExecutionListener(event -> {
            System.out.println(event);
            events.add(event);
        });
        controller.start();
        controller.terminate();
        Thread.sleep(5000);

        Assert.assertEquals(RunEvents.MissionException.class,events.get(1).getClass());
        Assert.assertEquals(IncompatibleMissionException.class,
                ((RunEvents.MissionException)events.get(1)).getThrowable().getClass());
        Assert.assertEquals("Mission must implement Runnable interface",
                ((RunEvents.MissionException)events.get(1)).getThrowable().getMessage());
    }


}