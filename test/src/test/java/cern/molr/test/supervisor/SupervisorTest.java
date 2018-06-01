package cern.molr.supervisor;

import cern.molr.api.supervisor.MoleSupervisor;
import cern.molr.commons.commands.Start;
import cern.molr.commons.commands.Terminate;
import cern.molr.commons.events.*;
import cern.molr.commons.mission.AnnotatedMissionMaterializer;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.commons.mission.Mission;
import cern.molr.commons.mission.MissionMaterializer;
import cern.molr.mole.spawner.MissionTest;
import cern.molr.commons.request.MissionCommandRequest;
import cern.molr.commons.response.MissionEvent;
import cern.molr.mole.supervisor.MoleSupervisor;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.supervisor.impl.supervisor.MoleSupervisorImpl;
import cern.molr.commons.request.server.SupervisorInstantiationRequest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Class for testing {@link MoleSupervisorImpl}
 * @author yassine-kr
 */
public class SupervisorTest {


    @Test
    public void instantiateTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(1);

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(MissionTest.class);
        List<MissionEvent> events=new ArrayList<>();

        MoleSupervisor supervisor=new MoleSupervisorImpl();
        supervisor.instantiate(mission,42,"1").subscribe(event -> {
            events.add(event);
            signal.countDown();
        });

        signal.await();
        Assert.assertEquals(1,events.size());
        Assert.assertEquals(SessionInstantiated.class,events.get(0).getClass());
    }

    @Test
    public void startFinishTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(4);

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission=materializer.materialize(MissionTest.class);
        List<MissionEvent> events=new ArrayList<>();

        MoleSupervisor supervisor=new MoleSupervisorImpl();
        supervisor.instantiate(mission,42,"1").subscribe(event -> {
            events.add(event);
            signal.countDown();
        });

        supervisor.instruct(new MissionCommandRequest("1",new Start()));

        signal.await();
        Assert.assertEquals(4,events.size());
        Assert.assertEquals(MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(SessionTerminated.class,events.get(3).getClass());
        Assert.assertEquals(84,((MissionFinished)events.get(2)).getResult());
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
        List<MissionEvent> events=new ArrayList<>();

        MoleSupervisor supervisor=new MoleSupervisorImpl();
        supervisor.instantiate(mission,42,"1").subscribe(event -> {
            events.add(event);
            signal.countDown();
        });
        supervisor.instruct(new MissionCommandRequest("1",new Start()));
        supervisor.instruct(new MissionCommandRequest("1",new Terminate()));

        signal.await();
        Assert.assertEquals(3,events.size());
    }

    @Test
    public void remoteTest() throws Exception {

        CountDownLatch instantiateSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(4);


        ConfigurableApplicationContext context =SpringApplication.run(RemoteSupervisorMain.class, new
                String[]{"--server.port=8080"});


        List<MissionEvent> events=new ArrayList<>();
        List<CommandResponse> responses=new ArrayList<>();

        SupervisorInstantiationRequest<Integer> request=new SupervisorInstantiationRequest<>("1",Fibonacci.class.getName(),42);
        MolrWebSocketClient client=new MolrWebSocketClient("localhost",8080);

        client.receiveFlux("/instantiate",MissionEvent.class,request).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(event)->{
            System.out.println("event: "+event);
            events.add(event);
            endSignal.countDown();

            if (event instanceof SessionInstantiated)
                instantiateSignal.countDown();
        }));

        instantiateSignal.await();

        client.receiveMono("/instruct",CommandResponse.class,new MissionCommandRequest("1",new Start())).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(result)->{
            System.out.println("response to start: "+result);
            responses.add(result);
        }));

        endSignal.await();

        Assert.assertEquals(4,events.size());
        Assert.assertEquals(SessionInstantiated.class,events.get(0).getClass());
        Assert.assertEquals(MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(SessionTerminated.class,events.get(3).getClass());
        Assert.assertEquals(1,responses.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,responses.get(0).getClass());

        SpringApplication.exit(context);

    }

}