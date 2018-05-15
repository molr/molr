package cern.molr.supervisor;

import cern.molr.commons.AnnotatedMissionMaterializer;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mission.Mission;
import cern.molr.mission.MissionMaterializer;
import cern.molr.mole.spawner.MissionTest;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleSupervisor;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.sample.mole.IntegerFunctionMole;
import cern.molr.supervisor.impl.MoleSupervisorImpl;
import cern.molr.supervisor.remote.RemoteSupervisorMain;
import cern.molr.supervisor.request.SupervisorMissionExecutionRequest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;

/**
 * Class for testing {@link MoleSupervisorImpl}
 * Each test can fail if the thread finishes before getting all results from supervisor, in that case sleep duration should be increased
 * @author yassine
 */
public class SupervisorTest {


    @Test
    public void InstantiateTest() throws Exception {

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(MissionTest.class);
        List<MoleExecutionEvent> events=new ArrayList<>();

        MoleSupervisor supervisor=new MoleSupervisorImpl();
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

        MoleSupervisor supervisor=new MoleSupervisorImpl();
        supervisor.instantiate(mission,42,"1").subscribe(event -> {
            events.add(event);
        });
        supervisor.instruct(new RunCommands.Start("1"));

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

        MoleSupervisor supervisor=new MoleSupervisorImpl();
        supervisor.instantiate(mission,42,"1").subscribe(event -> {
            events.add(event);
        });
        supervisor.instruct(new RunCommands.Start("1"));
        supervisor.instruct(new RunCommands.Terminate("1"));

        Thread.sleep(20000);
        Assert.assertEquals(3,events.size());
    }

    /**
     * To execute this test the supervisor server must be started at port 8080 (it is the default port defined in file "application.properties" of the module "supervisor")
     */
    @Test
    public void RemoteTest() throws Exception {

        ConfigurableApplicationContext contextSupervisor=SpringApplication.run(RemoteSupervisorMain.class,new String[]{"--server.port=8080"});
        Thread.sleep(10000);

        List<MoleExecutionEvent> events=new ArrayList<>();
        List<MoleExecutionCommandResponse> responses=new ArrayList<>();

        SupervisorMissionExecutionRequest<Integer> request=new SupervisorMissionExecutionRequest<>("1",IntegerFunctionMole.class.getCanonicalName(),Fibonacci.class.getCanonicalName(),42);
        MolrWebSocketClient client=new MolrWebSocketClient("localhost",8080);

        client.receiveFlux("/instantiate",MoleExecutionEvent.class,request).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(event)->{
            System.out.println("event: "+event);
            events.add(event);
        }));

        Thread.sleep( 4000);

        client.receiveMono("/instruct",MoleExecutionCommandResponse.class,new RunCommands.Start("1")).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(result)->{
            System.out.println("response to start: "+result);
            responses.add(result);
        }));

        Thread.sleep( 10000);

        Assert.assertEquals(3,events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(1,responses.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,responses.get(0).getClass());

        SpringApplication.exit(contextSupervisor);

    }

}