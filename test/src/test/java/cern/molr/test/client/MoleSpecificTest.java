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
import cern.molr.commons.events.MissionFinished;
import cern.molr.sample.commands.SequenceCommand;
import cern.molr.sample.events.SequenceMissionEvent;
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
 * Class for testing the client Api using mole specific commands, events and states.
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
     * A method which instantiates the {@link SequenceMissionExample} mission, and sends three specific commands; STEP,
     * SKIP and FINISH
     *
     * @param execName         the execution name used when displaying results
     * @param events           the events list which will be filled
     * @param commandResponses the command responses list which will be filled
     * @param states           the states list which will be filled
     * @param finishSignal     the signal to be triggered when all events and responses are received
     */
    private void launchSequenceMissionExample(String execName, List<MissionEvent> events,
                                              List<CommandResponse> commandResponses, List<MissionState> states,
                                              CountDownLatch finishSignal) {

        CountDownLatch instantiateSignal = new CountDownLatch(1);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch firstTaskSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(14);

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
                        } else if (event instanceof SequenceMissionEvent && ((SequenceMissionEvent) event).getEvent()
                                .equals(SequenceMissionEvent.Event.TASK_FINISHED) && ((SequenceMissionEvent) event)
                                .getTaskNumber() == 0)
                            firstTaskSignal.countDown();
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
                    firstTaskSignal.await();
                } catch (InterruptedException error) {
                    error.printStackTrace();
                    Assert.fail();
                }


                controller.instruct(new SequenceCommand(SequenceCommand.Command.SKIP))
                        .subscribe(new SimpleSubscriber<CommandResponse>() {
                            @Override
                            public void consume(CommandResponse response) {
                                System.out.println(execName + " response to skip: " + response);
                                commandResponses.add(response);
                                endSignal.countDown();

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
        List<MissionState> states = new ArrayList<>();
        CountDownLatch finishSignal = new CountDownLatch(1);

        launchSequenceMissionExample("exec", events, commandResponses, states, finishSignal);
        finishSignal.await();

        Assert.assertEquals(10, events.size());
        ResponseTester.testInstantiationEvent(events.get(0));
        ResponseTester.testStartedEvent(events.get(1));
        testTaskStarted(events.get(2), 0);
        testTaskFinished(events.get(3), 0);
        testTaskStarted(events.get(4), 2);
        testTaskError(events.get(5), 2);
        testTaskStarted(events.get(6), 3);
        testTaskFinished(events.get(7), 3);
        Assert.assertEquals(MissionFinished.class, events.get(8).getClass());
        ResponseTester.testTerminatedEvent(events.get(9));
        Assert.assertEquals(4, commandResponses.size());
        ResponseTester.testCommandResponseSuccess(commandResponses.get(0));
        ResponseTester.testCommandResponseSuccess(commandResponses.get(1));
        ResponseTester.testCommandResponseSuccess(commandResponses.get(2));
        ResponseTester.testCommandResponseSuccess(commandResponses.get(3));

        Assert.assertEquals(12, states.size());

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
        Assert.assertEquals(MissionState.Level.MOLE_RUNNER, states.get(10).getLevel());
        Assert.assertEquals("MISSION FINISHED", states.get(10).getStatus());
        Assert.assertArrayEquals(new MissionCommand[]{}, states.get(10).getPossibleCommands().toArray());
        Assert.assertEquals(MissionState.Level.MOLE_RUNNER, states.get(11).getLevel());
        Assert.assertEquals("SESSION TERMINATED", states.get(11).getStatus());
        Assert.assertArrayEquals(new MissionCommand[]{}, states.get(11).getPossibleCommands().toArray());

        testWaitingState(states.get(2), 0);
        testRunningState(states.get(3), 0);
        testWaitingState(states.get(4), 1);
        testWaitingState(states.get(5), 2);
        testRunningState(states.get(6), 2);
        testWaitingState(states.get(7), 3);
        testRunningState(states.get(8), 3);
        testFinishedState(states.get(9));
    }

    private void testTaskStarted(MissionEvent event, int taskNumer) {
        Assert.assertEquals(SequenceMissionEvent.class, event.getClass());
        Assert.assertEquals(SequenceMissionEvent.Event.TASK_STARTED, ((SequenceMissionEvent) event).getEvent());
        Assert.assertEquals(taskNumer, ((SequenceMissionEvent) event).getTaskNumber());
    }

    private void testTaskFinished(MissionEvent event, int taskNumer) {
        Assert.assertEquals(SequenceMissionEvent.class, event.getClass());
        Assert.assertEquals(SequenceMissionEvent.Event.TASK_FINISHED, ((SequenceMissionEvent) event).getEvent());
        Assert.assertEquals(taskNumer, ((SequenceMissionEvent) event).getTaskNumber());
    }

    private void testTaskError(MissionEvent event, int taskNumer) {
        Assert.assertEquals(SequenceMissionEvent.class, event.getClass());
        Assert.assertEquals(SequenceMissionEvent.Event.TASK_ERROR, ((SequenceMissionEvent) event).getEvent());
        Assert.assertEquals(taskNumer, ((SequenceMissionEvent) event).getTaskNumber());
    }

    private void testWaitingState(MissionState state, int taskNumber) {
        Assert.assertEquals(MissionState.Level.MOLE, state.getLevel());
        Assert.assertEquals("WAITING NEXT TASK " + taskNumber, state.getStatus());
        Assert.assertArrayEquals(new MissionCommand[]{new SequenceCommand(SequenceCommand.Command.STEP),
                        new SequenceCommand(SequenceCommand.Command.SKIP),
                        new SequenceCommand(SequenceCommand.Command.FINISH)},
                state.getPossibleCommands().toArray());
    }

    private void testRunningState(MissionState state, int taskNumber) {
        Assert.assertEquals(MissionState.Level.MOLE, state.getLevel());
        Assert.assertEquals("RUNNING TASK " + taskNumber, state.getStatus());
        Assert.assertArrayEquals(new MissionCommand[]{}, state.getPossibleCommands().toArray());
    }

    private void testFinishedState(MissionState state) {
        Assert.assertEquals(MissionState.Level.MOLE, state.getLevel());
        Assert.assertEquals("ALL TASKS FINISHED", state.getStatus());
        Assert.assertArrayEquals(new MissionCommand[]{}, state.getPossibleCommands().toArray());
    }

}
