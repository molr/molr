package cern.molr.client;

import cern.molr.mission.controller.ClientMissionController;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.sample.mission.Fibonacci;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Operator example
 * @author yassine
 */
public class SampleOperator {

    private MissionExecutionService service;

    public SampleOperator(MissionExecutionService service) {
        this.service = service;
    }

    /**
     * A method which instantiate a mission and terminate it
     * @param execName the name execution used when displaying results
     * @param missionClass the mission class
     * @param events the events list which will be filled
     * @param commandResponses the command responses list which will be filled
     * @param finishSignal the signal to be triggered when the all events and missions received
     * @throws Exception
     */
    private void launchMission(String execName,Class<?> missionClass,List<MoleExecutionEvent> events,
                               List<MoleExecutionCommandResponse>
                                       commandResponses,CountDownLatch finishSignal){

        CountDownLatch instantiateSignal = new CountDownLatch(1);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(5);

        Mono<ClientMissionController> futureController=service.instantiate(missionClass.getCanonicalName(),100);
        futureController.doOnError(Throwable::printStackTrace).subscribe((controller)->{
            controller.getFlux().subscribe((event)->{
                System.out.println(execName+" event: "+event);
                events.add(event);
                endSignal.countDown();
                if (event instanceof RunEvents.JVMInstantiated)
                    instantiateSignal.countDown();
                else if (event instanceof RunEvents.MissionStarted)
                    startSignal.countDown();
            });
            try {
                instantiateSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            controller.instruct(new RunCommands.Start()).subscribe((response)->{
                System.out.println(execName+" response to start: "+response);
                commandResponses.add(response);
                endSignal.countDown();
            });

            try {
                startSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.exit(-1);
            }
            controller.instruct(new RunCommands.Terminate()).subscribe((response)->{
                System.out.println(execName+" response to terminate: "+response);
                commandResponses.add(response);
                endSignal.countDown();
            });
        });
        new Thread(()->{
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

        List<MoleExecutionEvent> events1=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses1=new ArrayList<>();

        List<MoleExecutionEvent> events2=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses2=new ArrayList<>();

        launchMission("exec1",Fibonacci.class,events1,commandResponses1,finishSignal);
        launchMission("exec2",Fibonacci.class,events2,commandResponses2,finishSignal);
        finishSignal.await();

    }
}
