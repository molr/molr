package cern.molr.server;

import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionExecutionResponse;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.mole.supervisor.MoleExecutionEvent;
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

/**
 * Class for testing the server Api.
 * Each test can fail if the thread finishes before getting all results from supervisor, in that case sleep duration should be increased.
 *
 */
public class ServerTest {

    private ConfigurableApplicationContext contextServer;
    private ConfigurableApplicationContext contextSupervisor;

    @Before
    public void initServers() throws Exception{
        contextServer=SpringApplication.run(ServerMain.class, new String[]{"--server.port=8000"});
        Thread.sleep(10000);

        contextSupervisor=SpringApplication.run(RemoteSupervisorMain.class,new String[]{"--server.port=8056"});
        Thread.sleep(10000);
    }

    @After
    public void exitServers(){
        SpringApplication.exit(contextServer);
        SpringApplication.exit(contextSupervisor);
    }

    /**
     * To execute this test MolR Server must be started at port 8000 (it is the default port defined in file "application.properties" of the module "server")
     * Supervisor Server must be started just after to be registered in MolR Server
     */
    @Test
    public void InstantiateTest() throws Exception{

        List<MoleExecutionEvent> events=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses=new ArrayList<>();

        ServerMissionExecutionRequest<Integer> request=new ServerMissionExecutionRequest<>(Fibonacci.class.getCanonicalName(),23);
        MolrWebClient client=new MolrWebClient("localhost",8000);

        MissionExecutionResponse response=client.post("/instantiate", ServerMissionExecutionRequest.class, request, MissionExecutionResponse.class).get();
        Assert.assertEquals(MissionExecutionResponse.MissionExecutionResponseSuccess.class,response.getClass());

        MissionEventsRequest eventsRequest=new MissionEventsRequest(response.getResult().getMissionExecutionId());
        MolrWebSocketClient clientSocket=new MolrWebSocketClient("localhost",8000);
        clientSocket.receiveFlux("/getFlux",MoleExecutionEvent.class,eventsRequest).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(event)->{
            System.out.println("event: "+event);
            events.add(event);
        }));


        Thread.sleep(10000);
        System.out.println("sending start command");

        clientSocket.receiveMono("/instruct",MoleExecutionCommandResponse.class,new RunCommands.Start(response.getResult().getMissionExecutionId())).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(result)->{
            System.out.println("response to start: "+result);
            commandResponses.add(result);
        }));

        Thread.sleep( 20000);

        Assert.assertEquals(3,events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(1,commandResponses.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses.get(0).getClass());
    }
}
