package cern.molr.supervisor;

import cern.molr.commons.AnnotatedMissionMaterializer;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mission.Mission;
import cern.molr.mission.MissionMaterializer;
import cern.molr.mole.spawner.MissionTest;
import cern.molr.mole.spawner.debug.ResponseCommand;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionResponseCommand;
import cern.molr.mole.supervisor.MoleSupervisorNew;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.sample.mole.IntegerFunctionMole;
import cern.molr.supervisor.impl.MoleSupervisorImplNew;
import cern.molr.supervisor.request.MissionExecutionRequest;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for testing {@link MoleSupervisorImplNew}
 *
 * Each test can fail if the thread finishes before getting all results from supervisor, in that case sleep duration should be increased
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
        supervisor.instruct(new RunCommands.Start());

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
        supervisor.instruct(new RunCommands.Start());
        supervisor.instruct(new RunCommands.Terminate());

        Thread.sleep(20000);
        Assert.assertEquals(2,events.size());
    }

    /**
     * To run this test Supervisor Server must be started at port 8080 (it is defined in file "application.properties" of the module "supervisor")
     * @throws InterruptedException
     */
    @Test
    public void RemoteTest() throws InterruptedException {

        List<MoleExecutionEvent> events=new ArrayList<>();
        List<MoleExecutionResponseCommand> responses=new ArrayList<>();

        MissionExecutionRequest<Integer> request=new MissionExecutionRequest<>("1",IntegerFunctionMole.class.getCanonicalName(),Fibonacci.class.getCanonicalName(),42);
        MolrWebSocketClient client=new MolrWebSocketClient("localhost",8080);

        client.receiveFlux("/instantiate",MoleExecutionEvent.class,request).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(event)->{
            System.out.println("event: "+event);
            events.add(event);
        }));

        Thread.sleep( 4000);

        client.receiveMono("/instruct",MoleExecutionResponseCommand.class,new RunCommands.Start("1")).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(response)->{
            System.out.println("response to start: "+response);
            responses.add(response);
        }));

        Thread.sleep( 10000);

        Assert.assertEquals(3,events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(1,responses.size());
        Assert.assertEquals(ResponseCommand.ResponseCommandSuccess.class,responses.get(0).getClass());

    }



}