package cern.molr.supervisor;

import cern.molr.commons.AnnotatedMissionMaterializer;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mission.Mission;
import cern.molr.mission.MissionMaterializer;
import cern.molr.mole.spawner.MissionTest;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MissionCommandRequest;
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
import java.util.concurrent.CountDownLatch;

/**
 * Class for testing {@link MoleSupervisorImpl}
 * Each test can fail if the thread finishes before getting all results from supervisor, in that case sleep duration
 * should be increased
 * @author yassine-kr
 */
public class SupervisorTest {


    @Test
    public void instantiateTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(1);

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(MissionTest.class);
        List<MoleExecutionEvent> events=new ArrayList<>();

        MoleSupervisor supervisor=new MoleSupervisorImpl();
        supervisor.instantiate(mission,42,"1").subscribe(event -> {
            events.add(event);
            signal.countDown();
        });

        signal.await();
        Assert.assertEquals(1,events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
    }

    @Test
    public void startFinishTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(4);

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(MissionTest.class);
        List<MoleExecutionEvent> events=new ArrayList<>();

        MoleSupervisor supervisor=new MoleSupervisorImpl();
        supervisor.instantiate(mission,42,"1").subscribe(event -> {
            events.add(event);
            signal.countDown();
        });

        supervisor.instruct(new MissionCommandRequest("1",new RunCommands.Start()));

        signal.await();
        Assert.assertEquals(4,events.size());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(RunEvents.JVMDestroyed.class,events.get(3).getClass());
        Assert.assertEquals(84,((RunEvents.MissionFinished)events.get(2)).getResult());
    }

    /**
     * The mission execution should be long enough to terminate the JVM before the mission is finished
     * @throws Exception
     */
    @Test
    public void terminateTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(3);

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(MissionTest.class);
        List<MoleExecutionEvent> events=new ArrayList<>();

        MoleSupervisor supervisor=new MoleSupervisorImpl();
        supervisor.instantiate(mission,42,"1").subscribe(event -> {
            events.add(event);
            signal.countDown();
        });
        supervisor.instruct(new MissionCommandRequest("1",new RunCommands.Start()));
        supervisor.instruct(new MissionCommandRequest("1",new RunCommands.Terminate()));

        signal.await();
        Assert.assertEquals(3,events.size());
    }

    @Test
    public void remoteTest() throws Exception {

        CountDownLatch instantiateSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(4);


        ConfigurableApplicationContext context =SpringApplication.run(RemoteSupervisorMain.class, new
                String[]{"--server.port=8080"});


        List<MoleExecutionEvent> events=new ArrayList<>();
        List<MoleExecutionCommandResponse> responses=new ArrayList<>();

        SupervisorMissionExecutionRequest<Integer> request=new SupervisorMissionExecutionRequest<>("1",Fibonacci.class.getName(),42);
        MolrWebSocketClient client=new MolrWebSocketClient("localhost",8080);

        client.receiveFlux("/instantiate",MoleExecutionEvent.class,request).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(event)->{
            System.out.println("event: "+event);
            events.add(event);
            endSignal.countDown();

            if (event instanceof RunEvents.JVMInstantiated)
                instantiateSignal.countDown();
        }));

        instantiateSignal.await();

        client.receiveMono("/instruct",MoleExecutionCommandResponse.class,new MissionCommandRequest("1",new RunCommands.Start())).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(result)->{
            System.out.println("response to start: "+result);
            responses.add(result);
        }));

        endSignal.await();

        Assert.assertEquals(4,events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(RunEvents.JVMDestroyed.class,events.get(3).getClass());
        Assert.assertEquals(1,responses.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,responses.get(0).getClass());

        SpringApplication.exit(context);

    }

}