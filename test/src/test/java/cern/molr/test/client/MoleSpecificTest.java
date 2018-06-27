package cern.molr.test.client;

import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.client.impl.MissionExecutionServiceImpl;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.web.SimpleSubscriber;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.commons.events.MissionControlEvent;
import cern.molr.commons.events.MissionFinished;
import cern.molr.sample.commands.SequenceCommand;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.sample.mission.SequenceMissionExample;
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

import static cern.molr.commons.events.MissionControlEvent.Event.MISSION_STARTED;
import static cern.molr.commons.events.MissionControlEvent.Event.SESSION_INSTANTIATED;

/**
 * Class for testing client Api using mole specific commands, events and states
 *
 * @author yassine-kr
 */
public class MoleSpecificTest {

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
     * A method which instantiate the {@link SequenceMissionExample} mission, and send three specific commands; STEP,
     * SKIP and FINISH
     *
     * @param execName         the name execution used when displaying results
     * @param events           the events list which will be filled
     * @param commandResponses the command responses list which will be filled
     * @param finishSignal     the signal to be triggered when the all events and missions received
     *
     */
    private void launchSequenceMissionExample(String execName, List<MissionEvent> events,
                               List<CommandResponse> commandResponses, CountDownLatch finishSignal) {

        CountDownLatch instantiateSignal = new CountDownLatch(1);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(8);

        Publisher<ClientMissionController> futureController = service.instantiate(SequenceMissionExample.class.getName(),
        null);
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

                try {
                    instantiateSignal.await();
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
                    startSignal.await();
                } catch (InterruptedException error) {
                    error.printStackTrace();
                    Assert.fail();
                }

                controller.instruct(new SequenceCommand(SequenceCommand.Command.STEP))
                        .subscribe(new SimpleSubscriber<CommandResponse>() {
                    @Override
                    public void consume(CommandResponse response) {
                        System.out.println(execName + " response to step: " + response);
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
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                controller.instruct(new SequenceCommand(SequenceCommand.Command.SKIP))
                        .subscribe(new SimpleSubscriber<CommandResponse>() {
                            @Override
                            public void consume(CommandResponse response) {
                                System.out.println(execName + " response to skip: " + response);
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

                controller.instruct(new SequenceCommand(SequenceCommand.Command.FINISH))
                        .subscribe(new SimpleSubscriber<CommandResponse>() {
                            @Override
                            public void consume(CommandResponse response) {
                                System.out.println(execName + " response to finish: " + response);
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
                endSignal.await();
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
        CountDownLatch finishSignal = new CountDownLatch(1);

        launchSequenceMissionExample("exec", events, commandResponses, finishSignal);
        finishSignal.await();

        Assert.assertEquals(4, events.size());
        ResponseTester.testInstantiationEvent(events.get(0));
        ResponseTester.testStartedEvent(events.get(1));
        Assert.assertEquals(MissionFinished.class, events.get(2).getClass());
        ResponseTester.testTerminatedEvent(events.get(3));
        Assert.assertEquals(4, commandResponses.size());
        ResponseTester.testCommandResponseSuccess(commandResponses.get(0));
        ResponseTester.testCommandResponseSuccess(commandResponses.get(1));
        ResponseTester.testCommandResponseSuccess(commandResponses.get(2));
        ResponseTester.testCommandResponseSuccess(commandResponses.get(3));
    }

}
