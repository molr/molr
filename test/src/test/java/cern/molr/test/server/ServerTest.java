package cern.molr.test.server;

import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.request.client.ServerInstantiationRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.InstantiationResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.commons.events.MissionControlEvent;
import cern.molr.commons.events.MissionFinished;
import cern.molr.commons.web.MolrConfig;
import cern.molr.commons.web.WebFluxRestClient;
import cern.molr.commons.web.WebFluxWebSocketClient;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.server.ServerMain;
import cern.molr.supervisor.RemoteSupervisorMain;
import cern.molr.test.ResponseTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static cern.molr.commons.events.MissionControlEvent.Event.SESSION_INSTANTIATED;

/**
 * Class for testing the server Api.
 */
public class ServerTest {

    private ConfigurableApplicationContext serverContext;
    private ConfigurableApplicationContext supervisorContext;
    private WebFluxRestClient client = new WebFluxRestClient("http://localhost", 8000);
    private WebFluxWebSocketClient clientSocket = new WebFluxWebSocketClient("http://localhost", 8000);

    @Before
    public void initServers() {
        serverContext = SpringApplication.run(ServerMain.class, "--server.port=8000");

        supervisorContext = SpringApplication.run(RemoteSupervisorMain.class,
                "--server.port=8056", "--molr.host=http://localhost", "--molr.port=8000",
                "--supervisor.host=http://localhost", "--supervisor.port=8056");
    }

    @After
    public void exitServers() {
        SpringApplication.exit(supervisorContext);
        SpringApplication.exit(serverContext);
    }

    @Test
    public void instantiateTest() throws Exception {

        CountDownLatch instantiateSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(4);

        List<MissionEvent> events = new ArrayList<>();
        List<CommandResponse> commandResponses = new ArrayList<>();

        ServerInstantiationRequest<Integer> request = new ServerInstantiationRequest<>(
                Fibonacci.class.getCanonicalName(), 23);

        InstantiationResponse response = client.post(MolrConfig.INSTANTIATE_PATH, ServerInstantiationRequest.class, request,
                InstantiationResponse.class).block();

        Assert.assertTrue(response.isSuccess());


        clientSocket.receiveFlux(MolrConfig.EVENTS_STREAM_PATH, MissionEvent.class, response.getResult().getMissionId())
                .doOnError(Throwable::printStackTrace).subscribe(
                (event) -> {
                    System.out.println("event: " + event);
                    events.add(event);
                    endSignal.countDown();

                    if (event instanceof MissionControlEvent && ((MissionControlEvent) event).getEvent().equals
                            (SESSION_INSTANTIATED)) {
                        instantiateSignal.countDown();
                    }
                });


        instantiateSignal.await(1, TimeUnit.MINUTES);
        System.out.println("sending start command");

        clientSocket.receiveMono(MolrConfig.INSTRUCT_PATH, CommandResponse.class, new MissionCommandRequest(response
                .getResult().getMissionId(), new MissionControlCommand(MissionControlCommand.Command.START))).doOnError
                (Throwable::printStackTrace).subscribe((result) -> {
            System.out.println("response to start: " + result);
            commandResponses.add(result);
        });

        endSignal.await(1, TimeUnit.MINUTES);

        Assert.assertEquals(4, events.size());
        ResponseTester.testInstantiationEvent(events.get(0));
        ResponseTester.testStartedEvent(events.get(1));
        Assert.assertEquals(MissionFinished.class, events.get(2).getClass());
        ResponseTester.testTerminatedEvent(events.get(3));
        Assert.assertEquals(1, commandResponses.size());
        ResponseTester.testCommandResponseSuccess(commandResponses.get(0));
    }
}
