package cern.molr.test.client;

import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.client.impl.MissionExecutionServiceImpl;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import cern.molr.commons.api.web.SimpleSubscriber;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.commons.events.MissionControlEvent;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.server.ServerMain;
import cern.molr.supervisor.RemoteSupervisorMain;
import cern.molr.test.ResponseTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static cern.molr.commons.events.MissionControlEvent.Event.MISSION_STARTED;
import static cern.molr.commons.events.MissionControlEvent.Event.SESSION_INSTANTIATED;

/**
 * Class for testing client API allowing to get the mission states stream
 *
 * @author yassine-kr
 */
public class StatesTest {

    private ConfigurableApplicationContext serverContext;
    private ConfigurableApplicationContext supervisorContext;
    private MissionExecutionService service = new MissionExecutionServiceImpl("http://localhost", 8000);

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

    /**
     * A method which instantiates a mission and terminates it
     *
     * @param execName         the execution name used when displaying results
     * @param missionClass     the mission class
     * @param events           the events list which will be filled
     * @param commandResponses the command responses list which will be filled
     * @param states           the states list which will be filled
     * @param finishSignal     the signal to be triggered when all events and responses are received
     */
    private void launchMission(String execName, Class<?> missionClass, List<MissionEvent> events,
                               List<CommandResponse>
                                       commandResponses, List<MissionState> states, CountDownLatch finishSignal) {

        CountDownLatch instantiateSignal = new CountDownLatch(1);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(5);

        Publisher<ClientMissionController> futureController = service.instantiate(missionClass.getCanonicalName(), 100);
        futureController.subscribe(new SimpleSubscriber<ClientMissionController>() {

            @Override
            public void consume(ClientMissionController controller) {
                controller.getEventsStream().subscribe(new SimpleSubscriber<MissionEvent>() {

                    @Override
                    public void consume(MissionEvent event) {
                        System.out.println(execName + " event: " + event);
                        events.add(event);
                        endSignal.countDown();
                        if (event instanceof MissionControlEvent && ((MissionControlEvent) event).getEvent().equals(SESSION_INSTANTIATED)) {
                            instantiateSignal.countDown();
                        } else if (event instanceof MissionControlEvent && ((MissionControlEvent) event).getEvent()
                                .equals(MISSION_STARTED)) {
                            startSignal.countDown();
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

                controller.getStatesStream().subscribe(new SimpleSubscriber<MissionState>() {

                    @Override
                    public void consume(MissionState state) {
                        System.out.println(execName + " state: " + state);
                        states.add(state);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

                try {
                    instantiateSignal.await(1, TimeUnit.MINUTES);
                } catch (InterruptedException error) {
                    error.printStackTrace();
                    Assert.fail();
                }
                controller.instruct(new MissionControlCommand(MissionControlCommand.Command.START))
                        .subscribe(new SimpleSubscriber<CommandResponse>() {
                            @Override
                            public void consume(CommandResponse response) {
                                System.out.println(execName + " response to start: " + response);
                                commandResponses.add(response);
                                endSignal.countDown();
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                throwable.printStackTrace();
                            }

                            @Override
                            public void onComplete() {

                            }
                        });

                try {
                    startSignal.await(1, TimeUnit.MINUTES);
                } catch (InterruptedException error) {
                    error.printStackTrace();
                    Assert.fail();
                }

                controller.instruct(new MissionControlCommand(MissionControlCommand.Command.TERMINATE)).subscribe(new SimpleSubscriber<CommandResponse>() {
                    @Override
                    public void consume(CommandResponse response) {
                        System.out.println(execName + " response to terminate: " + response);
                        commandResponses.add(response);
                        endSignal.countDown();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {

            }
        });
        new Thread(() -> {
            try {
                endSignal.await(1, TimeUnit.MINUTES);
                finishSignal.countDown();
            } catch (InterruptedException error) {
                error.printStackTrace();
                Assert.fail();
            }

        }).start();

    }

    @Test
    public void missionTest() throws Exception {

        List<MissionEvent> events = new ArrayList<>();
        List<CommandResponse> commandResponses = new ArrayList<>();
        List<MissionState> states = new ArrayList<>();

        CountDownLatch finishSignal = new CountDownLatch(1);

        launchMission("exec", Fibonacci.class, events, commandResponses, states, finishSignal);
        finishSignal.await(1, TimeUnit.MINUTES);

        ResponseTester.testInstantiateStartTerminate(events, commandResponses);
        Assert.assertEquals(3, states.size());
        Assert.assertEquals(MissionState.Level.MOLE_RUNNER, states.get(0).getLevel());
        Assert.assertEquals("NOT YET STARTED", states.get(0).getStatus());
        Assert.assertArrayEquals(new MissionCommand[]{new MissionControlCommand(MissionControlCommand.Command.TERMINATE),
                        new MissionControlCommand(MissionControlCommand.Command.START)},
                states.get(0).getPossibleCommands().toArray());
        Assert.assertEquals(MissionState.Level.MOLE_RUNNER, states.get(1).getLevel());
        Assert.assertEquals("MISSION STARTED", states.get(1).getStatus());
        Assert.assertArrayEquals(new MissionCommand[]{new MissionControlCommand(MissionControlCommand.Command
                        .TERMINATE)},
                states.get(1).getPossibleCommands().toArray());
        Assert.assertEquals(MissionState.Level.MOLE_RUNNER, states.get(2).getLevel());
        Assert.assertEquals("SESSION TERMINATED", states.get(2).getStatus());
        Assert.assertArrayEquals(new MissionCommand[]{}, states.get(2).getPossibleCommands().toArray());
    }

}
