import cern.molr.commons.AnnotatedMissionMaterializer;
import cern.molr.exception.MissionMaterializationException;
import cern.molr.mission.Mission;
import cern.molr.mission.MissionMaterializer;
import cern.molr.mission.step.StepResult;
import cern.molr.mole.spawner.debug.DebugSpawner;
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

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

/**
 * Class for testing SupervisorsManagerImpl
 *
 * @author yassine
 */
public class TestSpawner {

    public static class C<T>{
        public <I,O> void f(I r) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
            O o=(O)Class.forName(String.class.getCanonicalName()).newInstance();
            System.out.println(o.getClass());

        }

        public void h(T a){
            System.out.println(a);
        }


    }


    @Test
    public void TestDebug() throws Exception {

        /*
        DebugSpawner<Integer> spawner=new DebugSpawner<Integer>();
        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(MissionTest.class);
        MoleSession session=spawner.spawnMoleRunner(mission,42);
        MoleExecutionController controller=session.getController();
        controller.addMoleExecutionListener(event -> System.out.println(event));
        while(true);
        */

        /*
        C c=new C();
        c.f(5);
        */

        //Class<C<Object>> c= (Class<C<Object>>) Class.forName(C.class.getCanonicalName());

    }

    @Test
    public void TestRun() throws Exception {


        RunSpawner<Integer> spawner=new RunSpawner<Integer>();
        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(MissionTest.class);
        MoleSession session=spawner.spawnMoleRunner(mission,42);
        MoleExecutionController controller=session.getController();
        controller.addMoleExecutionListener(event -> System.out.println(event));
        controller.start();
        controller.terminate();
        while(true);
    }


}