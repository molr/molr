package cern.molr.demo;


import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.web.SimpleSubscriber;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.commons.events.MissionControlEvent;
import cern.molr.sample.mission.Fibonacci;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static cern.molr.commons.events.MissionControlEvent.Event.MISSION_STARTED;
import static cern.molr.commons.events.MissionControlEvent.Event.SESSION_INSTANTIATED;

/**
 * Operator example
 *
 * @author yassine-kr
 */
public class ExampleOperator {

    private MissionExecutionService service;

    public ExampleOperator(MissionExecutionService service) {
        Objects.requireNonNull(service);
        this.service = service;
    }

    /**
     * A method which instantiates a mission and terminates it
     *
     * @param execName         the name execution used when displaying results
     * @param missionClass     the mission class
     * @param events           the events list which will be filled
     * @param commandResponses the command responses list which will be filled
     * @param finishSignal     the signal to be triggered when the all events and missions received
     *
     * @throws Exception
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
                    System.exit(-1);
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
                    System.exit(-1);
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
                endSignal.await();
                finishSignal.countDown();
            } catch (InterruptedException error) {
                error.printStackTrace();
                System.exit(-1);
            }

        }).start();
    }

    public void parallelExample() throws InterruptedException {
        CountDownLatch finishSignal = new CountDownLatch(2);

        List<MissionEvent> events1 = new ArrayList<>();
        List<CommandResponse> commandResponses1 = new ArrayList<>();

        List<MissionEvent> events2 = new ArrayList<>();
        List<CommandResponse> commandResponses2 = new ArrayList<>();

        launchMission("exec1", Fibonacci.class, events1, commandResponses1, finishSignal);
        launchMission("exec2", Fibonacci.class, events2, commandResponses2, finishSignal);

        finishSignal.await();

    }
}
