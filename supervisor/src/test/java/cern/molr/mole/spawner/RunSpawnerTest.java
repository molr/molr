package cern.molr.mole.spawner;

import cern.molr.commons.AnnotatedMissionMaterializer;
import cern.molr.exception.MissionMaterializationException;
import cern.molr.mission.Mission;
import cern.molr.mission.MissionMaterializer;
import cern.molr.mission.step.StepResult;
import cern.molr.mole.spawner.debug.DebugSpawner;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.spawner.run.RunSpawner;
import cern.molr.mole.supervisor.MoleExecutionController;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionListener;
import cern.molr.mole.supervisor.MoleSession;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.sample.mission.IntDoubler;
import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

/**
 * Class for testing SupervisorsManagerImpl
 *
 * Each test can fail if the thread finishes before getting all results from supervisor, in that case sleep duration should be increased
 *
 * @author yassine
 */
public class RunSpawnerTest {

    private <I> MoleExecutionController getController(Class<?> missionClass, I args) throws Exception {
        RunSpawner<I> spawner= new RunSpawner<>();
        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(missionClass);
        MoleSession session=spawner.spawnMoleRunner(mission,args);
        return session.getController();
    }

    @Test
    public void InstantiateTest() throws Exception {
        MoleExecutionController controller=getController(MissionTest.class,42);
        List<MoleExecutionEvent> events=new ArrayList<>();
        controller.addMoleExecutionListener(event -> {
           events.add(event);
        });
        Thread.sleep(20000);
        Assert.assertEquals(1,events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
    }

    @Test
    public void StartFinishTest() throws Exception {
        MoleExecutionController controller=getController(MissionTest.class,42);
        List<MoleExecutionEvent> events=new ArrayList<>();
        controller.addMoleExecutionListener(event -> {
            events.add(event);
        });
        controller.start();
        Thread.sleep(20000);
        Assert.assertEquals(3,events.size());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(84,((RunEvents.MissionFinished)events.get(2)).getResult());
    }

    @Test
    public void TerminateTest() throws Exception {
        MoleExecutionController controller=getController(MissionTest.class,42);
        List<MoleExecutionEvent> events=new ArrayList<>();
        controller.addMoleExecutionListener(event -> {
            events.add(event);
        });
        controller.start();
        controller.terminate();
        Thread.sleep(20000);
        Assert.assertEquals(2,events.size());
    }


}