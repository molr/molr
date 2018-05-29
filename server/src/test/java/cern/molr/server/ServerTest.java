package cern.molr.server;

import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionExecutionResponse;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MissionCommandRequest;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.server.request.MissionEventsRequest;
import cern.molr.server.request.ServerMissionExecutionRequest;
import cern.molr.supervisor.remote.RemoteSupervisorMain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Class for testing the server Api.
 * Each test can fail if the thread finishes before getting all results from supervisor,
 * in that case sleep duration should be increased.
 *
 */
public class ServerTest {

    private ConfigurableApplicationContext serverContext;
    private ConfigurableApplicationContext supervisorContext;
    private MolrWebClient client=new MolrWebClient("localhost",8000);
    private MolrWebSocketClient clientSocket=new MolrWebSocketClient("localhost",8000);

    @Before
    public void initServers(){
        serverContext =SpringApplication.run(ServerMain.class, new String[]{"--server.port=8000"});

        supervisorContext =SpringApplication.run(RemoteSupervisorMain.class,
                new String[]{"--server.port=8056","--molr.host=localhost","--molr.port=8000"});;
    }

    @After
    public void exitServers(){
        SpringApplication.exit(serverContext);
        SpringApplication.exit(supervisorContext);
    }

    @Test
    public void instantiateTest() throws Exception{

        CountDownLatch instantiateSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(4);

        List<MoleExecutionEvent> events=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses=new ArrayList<>();

        ServerMissionExecutionRequest<Integer> request=new ServerMissionExecutionRequest<>(
                Fibonacci.class.getCanonicalName(),23);

        MissionExecutionResponse response=client.post("/instantiate", ServerMissionExecutionRequest.class, request,
                MissionExecutionResponse.class).get();

        Assert.assertEquals(MissionExecutionResponse.MissionExecutionResponseSuccess.class,response.getClass());



        MissionEventsRequest eventsRequest=new MissionEventsRequest(response.getResult().getMissionExecutionId());
        clientSocket.receiveFlux("/getFlux",MoleExecutionEvent.class,eventsRequest)
                .doOnError(Throwable::printStackTrace).subscribe(
                        tryElement->tryElement.execute(Throwable::printStackTrace,(event)->{
                            System.out.println("event: "+event);
                            events.add(event);
                            endSignal.countDown();

                            if (event instanceof RunEvents.JVMInstantiated)
                                instantiateSignal.countDown();
        }));


        instantiateSignal.await();
        System.out.println("sending start command");

        clientSocket.receiveMono("/instruct",MoleExecutionCommandResponse.class,new MissionCommandRequest(response
                .getResult().getMissionExecutionId(),new RunCommands.Start())).doOnError
                (Throwable::printStackTrace).subscribe(tryElement->tryElement
                .execute(Throwable::printStackTrace,(result)->{
            System.out.println("response to start: "+result);
            commandResponses.add(result);
        }));

        endSignal.await();

        Assert.assertEquals(4,events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(RunEvents.JVMDestroyed.class,events.get(3).getClass());
        Assert.assertEquals(1,commandResponses.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses.get(0).getClass());
    }
}
