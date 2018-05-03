package cern.molr.server;

import cern.molr.commons.response.MissionExecutionResponse;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mole.spawner.debug.ResponseCommand;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionResponseCommand;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.sample.mole.IntegerFunctionMole;
import cern.molr.server.request.MissionEventsRequest;
import cern.molr.server.request.MissionExecutionRequest;
import cern.molr.type.Try;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Class for testing server api
 *
 * Each test can fail if the thread finishes before getting all results from supervisor, in that case sleep duration should be increased
 *
 */
public class ServerTest {

    /**
     * To run this test MolR Server must be started at port 8000 (it is defined in file "application.properties" of the module "server")
     * Supervisor Server must be started after to be registered in MolR
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void InstantiateTest() throws InterruptedException, ExecutionException {

        List<MoleExecutionEvent> events=new ArrayList<>();
        List<MoleExecutionResponseCommand> commandResponses=new ArrayList<>();

        MissionExecutionRequest<Integer> request=new MissionExecutionRequest<>(Fibonacci.class.getCanonicalName(),23);
        MolrWebClient client=new MolrWebClient("localhost",8000);

        MissionExecutionResponse response=client.post("/instantiate", MissionExecutionRequest.class, request, MissionExecutionResponse.class).get();
        Assert.assertEquals(MissionExecutionResponse.MissionExecutionResponseSuccess.class,response.getClass());

        MissionEventsRequest eventsRequest=new MissionEventsRequest(response.getResult().getMissionExecutionId());
        MolrWebSocketClient clientSocket=new MolrWebSocketClient("localhost",8000);
        clientSocket.receiveFlux("/getFlux",MoleExecutionEvent.class,eventsRequest).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(event)->{
            System.out.println("event: "+event);
            events.add(event);
        }));





        Thread.sleep(10000);
        System.out.println("sending start command");

        clientSocket.receiveMono("/instruct",MoleExecutionResponseCommand.class,new RunCommands.Start(response.getResult().getMissionExecutionId())).doOnError(Throwable::printStackTrace).subscribe(tryElement->tryElement.execute(Throwable::printStackTrace,(result)->{
            System.out.println("response to start: "+result);
            commandResponses.add(result);
        }));

        Thread.sleep( 20000);

        Assert.assertEquals(3,events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.MissionFinished.class,events.get(2).getClass());
        Assert.assertEquals(1,commandResponses.size());
        Assert.assertEquals(ResponseCommand.ResponseCommandSuccess.class,commandResponses.get(0).getClass());
    }
}
