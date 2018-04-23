package cern.molr.supervisor;

import cern.molr.commons.AnnotatedMissionMaterializer;
import cern.molr.mission.Mission;
import cern.molr.mission.MissionMaterializer;
import cern.molr.mole.spawner.MissionTest;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleSupervisorNew;
import cern.molr.supervisor.impl.MoleSupervisorImplNew;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for testing {@link MoleSupervisorImplNew}
 *
 * @author yassine
 */
public class SupervisorTest {


    @Test
    public void InstantiateTest() throws Exception {

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(MissionTest.class);
        List<MoleExecutionEvent> events=new ArrayList<>();

        MoleSupervisorNew supervisor=new MoleSupervisorImplNew();
        supervisor.instantiate(mission,42,"1").subscribe(event -> {
            events.add(event);
        });

        Thread.sleep(20000);
        Assert.assertEquals(1,events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
    }

    @Test
    public void StartFinishTest() throws Exception {

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(MissionTest.class);
        List<MoleExecutionEvent> events=new ArrayList<>();

        MoleSupervisorNew supervisor=new MoleSupervisorImplNew();
        supervisor.instantiate(mission,42,"1").subscribe(event -> {
            events.add(event);
        });
        supervisor.instruct(new RunCommands.Start(),"1");

        Thread.sleep(20000);
        Assert.assertEquals(3,events.size());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(84,((RunEvents.MissionFinished)events.get(2)).getResult());
    }

    @Test
    public void TerminateTest() throws Exception {

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(MissionTest.class);
        List<MoleExecutionEvent> events=new ArrayList<>();

        MoleSupervisorNew supervisor=new MoleSupervisorImplNew();
        supervisor.instantiate(mission,42,"1").subscribe(event -> {
            events.add(event);
        });
        supervisor.instruct(new RunCommands.Start(),"1");
        supervisor.instruct(new RunCommands.Terminate(),"1");

        Thread.sleep(20000);
        Assert.assertEquals(2,events.size());
    }



}