package cern.molr.test.client;

import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.client.impl.MissionExecutionServiceImpl;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.web.SimpleSubscriber;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.commons.events.MissionRunnerEvent;
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

import static cern.molr.commons.events.MissionRunnerEvent.Event.MISSION_STARTED;
import static cern.molr.commons.events.MissionRunnerEvent.Event.SESSION_INSTANTIATED;

/**
 * Class for testing the client Api.
 * TODO find a working way to launch many supervisors on the same execution, exiting an app context disallows any
 * TODO further connections
 * @author yassine-kr
 */
public class ClientTest {

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
     * @param finishSignal     the signal to be triggered when all events and responses are received
     */
    private void launchMission(String execName, Class<?> missionClass, List<MissionEvent> events,
                               List<CommandResponse>
                                       commandResponses, CountDownLatch finishSignal) {

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
                        if (event instanceof MissionRunnerEvent && ((MissionRunnerEvent) event).getEvent().equals(SESSION_INSTANTIATED)) {
                            instantiateSignal.countDown();
                        } else if (event instanceof MissionRunnerEvent && ((MissionRunnerEvent) event).getEvent()
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

                try {
                    instantiateSignal.await(1, TimeUnit.MINUTES);
                } catch (InterruptedException error) {
                    error.printStackTrace();
                    Assert.fail();
                }
                controller.instruct(new MissionControlCommand(MissionControlCommand.Command.START)).subscribe(new
                                                                                                                      SimpleSubscriber<CommandResponse>() {
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

    /**
     * The mission execution should be long enough to terminate the session before the mission is finished
     *
     */
    @Test
    public void missionTest() throws Exception {

        List<MissionEvent> events = new ArrayList<>();
        List<CommandResponse> commandResponses = new ArrayList<>();
        CountDownLatch finishSignal = new CountDownLatch(1);

        launchMission("exec", Fibonacci.class, events, commandResponses, finishSignal);
        finishSignal.await(1, TimeUnit.MINUTES);

        ResponseTester.testInstantiateStartTerminate(events, commandResponses);
    }

    /**
     * The mission execution should be long enough to terminate the session before the mission is finished
     * Testing a sequential missions execution
     */
    @Test
    public void missionsTest() throws Exception {

        CountDownLatch finishSignal1 = new CountDownLatch(1);

        List<MissionEvent> events1 = new ArrayList<>();
        List<CommandResponse> commandResponses1 = new ArrayList<>();

        launchMission("exec1", Fibonacci.class, events1, commandResponses1, finishSignal1);
        finishSignal1.await(1, TimeUnit.MINUTES);

        List<MissionEvent> events2 = new ArrayList<>();
        List<CommandResponse> commandResponses2 = new ArrayList<>();

        CountDownLatch instantiateSignal2 = new CountDownLatch(1);
        CountDownLatch startSignal2 = new CountDownLatch(2);
        CountDownLatch endSignal2 = new CountDownLatch(6);

        CountDownLatch startCommandSignal = new CountDownLatch(1);


        Publisher<ClientMissionController> futureController2 =
                service.instantiate(Fibonacci.class.getCanonicalName(), 100);


        futureController2.subscribe(new SimpleSubscriber<ClientMissionController>() {

            @Override
            public void consume(ClientMissionController controller) {
                controller.getEventsStream().subscribe(new SimpleSubscriber<MissionEvent>() {

                    @Override
                    public void consume(MissionEvent event) {
                        System.out.println("exec2 event: " + event);
                        events2.add(event);
                        endSignal2.countDown();

                        if (event instanceof MissionRunnerEvent && ((MissionRunnerEvent) event).getEvent().equals(SESSION_INSTANTIATED)) {
                            instantiateSignal2.countDown();
                        } else if (event instanceof MissionRunnerEvent && ((MissionRunnerEvent) event).getEvent()
                                .equals(MISSION_STARTED)) {
                            startSignal2.countDown();
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

                try {
                    instantiateSignal2.await(1, TimeUnit.MINUTES);
                } catch (InterruptedException error) {
                    error.printStackTrace();
                    Assert.fail();
                }

                controller.instruct(new MissionControlCommand(MissionControlCommand.Command.START)).subscribe(new SimpleSubscriber<CommandResponse>() {
                    @Override
                    public void consume(CommandResponse response) {
                        System.out.println("exec2 response to start: " + response);
                        commandResponses2.add(response);
                        endSignal2.countDown();
                        startCommandSignal.countDown();
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
                    startCommandSignal.await(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Assert.fail();
                }

                controller.instruct(new MissionControlCommand(MissionControlCommand.Command.START)).subscribe(new SimpleSubscriber<CommandResponse>() {
                    @Override
                    public void consume(CommandResponse response) {
                        System.out.println("exec2 response to start 2: " + response);
                        commandResponses2.add(response);
                        endSignal2.countDown();
                        startSignal2.countDown();
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
                    startSignal2.await(1, TimeUnit.MINUTES);
                } catch (InterruptedException error) {
                    error.printStackTrace();
                    Assert.fail();
                }

                controller.instruct(new MissionControlCommand(MissionControlCommand.Command.TERMINATE)).subscribe(new SimpleSubscriber<CommandResponse>() {
                    @Override
                    public void consume(CommandResponse response) {
                        System.out.println("exec2 response to terminate: " + response);
                        commandResponses2.add(response);
                        endSignal2.countDown();
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

        endSignal2.await(1, TimeUnit.MINUTES);

        CountDownLatch finishSignal3 = new CountDownLatch(1);
        List<MissionEvent> events3 = new ArrayList<>();
        List<CommandResponse> commandResponses3 = new ArrayList<>();


        launchMission("exec3", Fibonacci.class, events3, commandResponses3, finishSignal3);
        finishSignal3.await(1, TimeUnit.MINUTES);

        ResponseTester.testInstantiateStartTerminate(events1, commandResponses1);


        Assert.assertEquals(3, events2.size());
        ResponseTester.testInstantiationEvent(events2.get(0));
        ResponseTester.testStartedEvent(events2.get(1));
        ResponseTester.testTerminatedEvent(events2.get(2));
        Assert.assertEquals(3, commandResponses2.size());
        ResponseTester.testCommandResponseSuccess(commandResponses2.get(0));
        ResponseTester.testCommandResponseFailure(commandResponses2.get(1));
        ResponseTester.testCommandResponseSuccess(commandResponses2.get(2));


    }

    /**
     * The mission execution should be long enough to terminate the session before the mission is finished
     */
    @Test
    public void parallelMissionsTest() throws Exception {

        CountDownLatch finishSignal = new CountDownLatch(2);

        List<MissionEvent> events1 = new ArrayList<>();
        List<CommandResponse> commandResponses1 = new ArrayList<>();

        List<MissionEvent> events2 = new ArrayList<>();
        List<CommandResponse> commandResponses2 = new ArrayList<>();

        launchMission("exec1", Fibonacci.class, events1, commandResponses1, finishSignal);
        launchMission("exec2", Fibonacci.class, events2, commandResponses2, finishSignal);
        finishSignal.await(1, TimeUnit.MINUTES);


        ResponseTester.testInstantiateStartTerminate(events1, commandResponses1);

        ResponseTester.testInstantiateStartTerminate(events2, commandResponses2);

    }
}
