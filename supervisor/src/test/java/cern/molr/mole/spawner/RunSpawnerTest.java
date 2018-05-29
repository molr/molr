package cern.molr.mole.spawner;

import cern.molr.commons.AnnotatedMissionMaterializer;
import cern.molr.mission.Mission;
import cern.molr.mission.MissionMaterializer;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunController;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.spawner.run.RunSpawner;
import cern.molr.mole.supervisor.MoleExecutionController;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MissionSession;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * class for testing {@link RunController}
 * @author yassine-kr
 */
public class RunSpawnerTest {

    private <I> MoleExecutionController getController(Class<?> missionClass, I args) throws Exception {
        RunSpawner<I> spawner= new RunSpawner<>();
        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(missionClass);
        MissionSession session=spawner.spawnMoleRunner(mission,args);
        return session.getController();
    }

    @Test
    public void InstantiateTest() throws Exception {

        CountDownLatch signal = new CountDownLatch(1);

        MoleExecutionController controller=getController(MissionTest.class,42);
        List<MoleExecutionEvent> events=new ArrayList<>();
        controller.addMoleExecutionListener(event -> {
           events.add(event);
           signal.countDown();
        });
        signal.await();
        Assert.assertEquals(1,events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
    }

    @Test
    public void StartFinishTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(4);

        MoleExecutionController controller=getController(MissionTest.class,42);
        List<MoleExecutionEvent> events=new ArrayList<>();
        controller.addMoleExecutionListener(event -> {
            events.add(event);
            signal.countDown();
        });
        controller.sendCommand(new RunCommands.Start());
        signal.await();
        Assert.assertEquals(4,events.size());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(RunEvents.JVMDestroyed.class,events.get(3).getClass());
        Assert.assertEquals(84,((RunEvents.MissionFinished)events.get(2)).getResult());
    }

    /**
     * The mission execution should be long enough to terminate the JVM before the the mission is finished
     * @throws Exception
     */
    @Test
    public void TerminateTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(3);

        MoleExecutionController controller=getController(MissionTest.class,42);
        List<MoleExecutionEvent> events=new ArrayList<>();
        controller.addMoleExecutionListener(event -> {
            System.out.println(event);
            events.add(event);
            signal.countDown();
        });
        controller.sendCommand(new RunCommands.Start());
        controller.sendCommand(new RunCommands.Terminate());
        signal.await();
        Assert.assertEquals(3,events.size());
        Assert.assertEquals(RunEvents.JVMDestroyed.class,events.get(2).getClass());
    }


}