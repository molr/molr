package cern.molr.test.supervisor;

import cern.molr.commons.api.mission.Mission;
import cern.molr.commons.api.mission.MissionMaterializer;
import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.request.server.InstantiationRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.web.SimpleSubscriber;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.commons.events.MissionControlEvent;
import cern.molr.commons.events.MissionFinished;
import cern.molr.commons.events.MissionStateEvent;
import cern.molr.commons.impl.mission.AnnotatedMissionMaterializer;
import cern.molr.commons.web.MolrConfig;
import cern.molr.commons.web.WebFluxWebSocketClient;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.supervisor.RemoteSupervisorMain;
import cern.molr.supervisor.api.supervisor.MoleSupervisor;
import cern.molr.supervisor.impl.supervisor.MoleSupervisorImpl;
import cern.molr.test.MissionTest;
import cern.molr.test.ResponseTester;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static cern.molr.commons.events.MissionControlEvent.Event.SESSION_INSTANTIATED;

/**
 * Class for testing {@link MoleSupervisorImpl}
 *
 * @author yassine-kr
 */
public class SupervisorTest {


    @Test
    public void instantiateTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(1);

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission = materializer.materialize(MissionTest.class.getName());
        List<MissionEvent> events = new ArrayList<>();

        MoleSupervisor supervisor = new MoleSupervisorImpl();
        supervisor.instantiate(mission, 42, "1").subscribe(new SimpleSubscriber<MissionEvent>() {

            @Override
            public void consume(MissionEvent event) {
                if (event instanceof MissionStateEvent) {
                    return;
                }
                events.add(event);
                signal.countDown();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });

        signal.await();
        Assert.assertEquals(1, events.size());
        ResponseTester.testInstantiationEvent(events.get(0));
    }

    @Test
    public void startFinishTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(4);

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission = materializer.materialize(MissionTest.class.getName());
        List<MissionEvent> events = new ArrayList<>();

        MoleSupervisor supervisor = new MoleSupervisorImpl();
        supervisor.instantiate(mission, 42, "1").subscribe(new SimpleSubscriber<MissionEvent>() {

            @Override
            public void consume(MissionEvent event) {
                if (event instanceof MissionStateEvent) {
                    return;
                }
                events.add(event);
                signal.countDown();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });

        supervisor.instruct(new MissionCommandRequest("1", new MissionControlCommand(MissionControlCommand.Command.START)));

        signal.await();
        Assert.assertEquals(4, events.size());
        ResponseTester.testStartedEvent(events.get(1));
        Assert.assertEquals(MissionFinished.class, events.get(2).getClass());
        ResponseTester.testTerminatedEvent(events.get(3));
        Assert.assertEquals(84, ((MissionFinished) events.get(2)).getResult());
    }

    /**
     * The mission execution should be long enough to terminate the session before the mission is finished
     */
    @Test
    public void terminateTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(3);

        MissionMaterializer materializer = new AnnotatedMissionMaterializer();
        Mission mission = materializer.materialize(MissionTest.class.getName());
        List<MissionEvent> events = new ArrayList<>();

        MoleSupervisor supervisor = new MoleSupervisorImpl();
        supervisor.instantiate(mission, 42, "1").subscribe(new SimpleSubscriber<MissionEvent>() {

            @Override
            public void consume(MissionEvent event) {
                if (event instanceof MissionStateEvent) {
                    return;
                }
                events.add(event);
                signal.countDown();
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });
        supervisor.instruct(new MissionCommandRequest("1", new MissionControlCommand(MissionControlCommand.Command.START)));
        supervisor.instruct(new MissionCommandRequest("1", new MissionControlCommand(MissionControlCommand.Command.TERMINATE)));

        signal.await();
        Assert.assertEquals(3, events.size());
    }

    @Test
    public void remoteTest() throws Exception {

        CountDownLatch instantiateSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(4);


        ConfigurableApplicationContext context = SpringApplication.run(RemoteSupervisorMain.class, "--server.port=8080");


        List<MissionEvent> events = new ArrayList<>();
        List<CommandResponse> responses = new ArrayList<>();

        InstantiationRequest<Integer> request =
                new InstantiationRequest<>("1", Fibonacci.class.getName(), 42);
        WebFluxWebSocketClient client = new WebFluxWebSocketClient("http://localhost", 8080);

        client.receiveFlux(MolrConfig.INSTANTIATE_PATH, MissionEvent.class, request)
                .doOnError(Throwable::printStackTrace)
                .subscribe((event) -> {
                    if (event instanceof MissionStateEvent) {
                        return;
                    }
                    System.out.println("event: " + event);
                    events.add(event);
                    endSignal.countDown();

                    if (event instanceof MissionControlEvent && ((MissionControlEvent) event).getEvent().equals(SESSION_INSTANTIATED)) {
                        instantiateSignal.countDown();
                    }
                });

        instantiateSignal.await();

        client.receiveMono(MolrConfig.INSTRUCT_PATH, CommandResponse.class, new MissionCommandRequest("1", new MissionControlCommand(MissionControlCommand.Command.START)))
                .doOnError(Throwable::printStackTrace)
                .subscribe((result) -> {
                    System.out.println("response to start: " + result);
                    responses.add(result);
                });

        endSignal.await();

        Assert.assertEquals(4, events.size());
        ResponseTester.testInstantiationEvent(events.get(0));
        ResponseTester.testStartedEvent(events.get(1));
        Assert.assertEquals(MissionFinished.class, events.get(2).getClass());
        ResponseTester.testTerminatedEvent(events.get(3));
        Assert.assertEquals(1, responses.size());
        ResponseTester.testCommandResponseSuccess(responses.get(0));

        SpringApplication.exit(context);

    }

}