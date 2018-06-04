package cern.molr.client;


import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.commons.commands.Start;
import cern.molr.commons.commands.Terminate;
import cern.molr.commons.events.MissionStarted;
import cern.molr.commons.events.SessionInstantiated;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionEvent;
import cern.molr.sample.mission.Fibonacci;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Operator example
 *
 * @author yassine
 */
public class SampleOperator {

    private MissionExecutionService service;

    public SampleOperator(MissionExecutionService service) {
        this.service = service;
    }

    /**
     * A method which instantiate a mission and terminate it
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

        Mono<ClientMissionController> futureController = service.instantiate(missionClass.getCanonicalName(), 100);
        futureController.doOnError(Throwable::printStackTrace).subscribe((controller) -> {
            controller.getFlux().subscribe((event) -> {
                System.out.println(execName + " event: " + event);
                events.add(event);
                endSignal.countDown();
                if (event instanceof SessionInstantiated)
                    instantiateSignal.countDown();
                else if (event instanceof MissionStarted)
                    startSignal.countDown();
            });
            try {
                instantiateSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            controller.instruct(new Start()).subscribe((response) -> {
                System.out.println(execName + " response to start: " + response);
                commandResponses.add(response);
                endSignal.countDown();
            });

            try {
                startSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            controller.instruct(new Terminate()).subscribe((response) -> {
                System.out.println(execName + " response to terminate: " + response);
                commandResponses.add(response);
                endSignal.countDown();
            });
        });
        new Thread(() -> {
            try {
                endSignal.await();
                finishSignal.countDown();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
