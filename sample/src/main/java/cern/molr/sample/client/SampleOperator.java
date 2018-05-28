package cern.molr.sample.client;

import cern.molr.commons.response.CommandResponse;
import cern.molr.mission.controller.ClientMissionController;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.sample.mission.Fibonacci;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Operator example
 * @author yassine
 */
public class SampleOperator {

    private MissionExecutionService service;

    public SampleOperator(MissionExecutionService service) {
        this.service = service;
    }

    public void parallelExample() throws InterruptedException {
        List<MoleExecutionEvent> events1=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses1=new ArrayList<>();

        List<MoleExecutionEvent> events2=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses2=new ArrayList<>();

        Mono<ClientMissionController> futureController1=
                service.instantiate(Fibonacci.class.getCanonicalName(),100);


        
        futureController1.doOnError(Throwable::printStackTrace).subscribe((controller)->{


            controller.getFlux().subscribe((event)->{
                System.out.println("event(1): "+event);
                events1.add(event);
            });


            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            controller.instruct(new RunCommands.Start()).subscribe((response)->{
                System.out.println("response(1) to start: "+response);
                commandResponses1.add(response);
            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            controller.instruct(new RunCommands.Terminate()).subscribe((response)->{
                System.out.println("response(1) to terminate: "+response);
                commandResponses1.add(response);
            });


        });



        Mono<ClientMissionController> futureController2=
                service.instantiate(Fibonacci.class.getCanonicalName(),100);



        futureController2.doOnError(Throwable::printStackTrace).subscribe((controller)->{
            controller.getFlux().subscribe((event)->{
                System.out.println("event(2): "+event);
                events2.add(event);
            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            controller.instruct(new RunCommands.Start()).subscribe((response)->{
                System.out.println("response(2) to start: "+response);
                commandResponses2.add(response);
            });

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.instruct(new RunCommands.Terminate()).subscribe((response)->{
                System.out.println("response(2) to terminate: "+response);
                commandResponses2.add(response);
            });
        });


        Thread.sleep(10000);



    }
}
