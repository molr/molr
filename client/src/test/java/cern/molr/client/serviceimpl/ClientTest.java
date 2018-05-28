package cern.molr.client.serviceimpl;

import cern.molr.mission.controller.ClientMissionController;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.commons.response.CommandResponse;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.server.ServerMain;
import cern.molr.supervisor.remote.RemoteSupervisorMain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class for testing client Api.
 * Each test can fail if the thread finishes before getting all results from the supervisor, in that case the sleep
 * duration should be increased
 *
 * @author yassine-kr
 */
public class ClientTest {

    private ConfigurableApplicationContext contextServer;
    private ConfigurableApplicationContext contextSupervisor;
    private MissionExecutionService service=new MissionExecutionServiceImpl("localhost",8000);

    @Before
    public void initServers() throws Exception{
        contextServer=SpringApplication.run(ServerMain.class, new String[]{"--server.port=8000"});
        Thread.sleep(10000);

        contextSupervisor=SpringApplication.run(RemoteSupervisorMain.class,
                new String[]{"--server.port=8056","--molr.host=localhost","--molr.port=8000"});
        Thread.sleep(10000);
    }

    @After
    public void exitServers(){
        SpringApplication.exit(contextServer);
        SpringApplication.exit(contextSupervisor);
    }

    @Test
    public void missionTest() throws Exception {

        List<MoleExecutionEvent> events=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses=new ArrayList<>();

        Mono<ClientMissionController> futureController=service.instantiate(Fibonacci.class.getCanonicalName(),100);
        futureController.doOnError(Throwable::printStackTrace).subscribe((controller)->{
           controller.getFlux().subscribe((event)->{
               System.out.println("event: "+event);
               events.add(event);
           });
           try {
               Thread.sleep(2000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           controller.instruct(new RunCommands.Start()).subscribe((response)->{
               System.out.println("response to start: "+response);
               commandResponses.add(response);
           });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.instruct(new RunCommands.Terminate()).subscribe((response)->{
               System.out.println("response to terminate: "+response);
               commandResponses.add(response);
           });
        });
        Thread.sleep(10000);

        Assert.assertEquals(3, events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.JVMDestroyed.class,events.get(2).getClass());
        Assert.assertEquals(2,commandResponses.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses.get(0).getClass());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses.get(1).getClass());
    }

    @Test
    public void missionsTest() throws Exception {

        List<MoleExecutionEvent> events1=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses1=new ArrayList<>();

        List<MoleExecutionEvent> events2=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses2=new ArrayList<>();

        List<MoleExecutionEvent> events3=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses3=new ArrayList<>();

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

        Thread.sleep(5000);


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
            controller.instruct(new RunCommands.Start()).subscribe((response)->{
                System.out.println("response(2) to start 2: "+response);
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

        Thread.sleep(5000);


        Mono<ClientMissionController> futureController3=
                service.instantiate(Fibonacci.class.getCanonicalName(),100);


        futureController3.doOnError(Throwable::printStackTrace).subscribe((controller)->{
            controller.getFlux().subscribe((event)->{
                System.out.println("event(3): "+event);
                events3.add(event);
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.instruct(new RunCommands.Start()).subscribe((response)->{
                System.out.println("response(3) to start: "+response);
                commandResponses3.add(response);
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.instruct(new RunCommands.Terminate()).subscribe((response)->{
                System.out.println("response(3) to terminate: "+response);
                commandResponses3.add(response);
            });
        });



        Thread.sleep(5000);

        Assert.assertEquals(3, events1.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events1.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events1.get(1).getClass());
        Assert.assertEquals(RunEvents.JVMDestroyed.class,events1.get(2).getClass());
        Assert.assertEquals(2,commandResponses1.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses1.get(0).getClass());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses1.get(1).getClass());


        Assert.assertEquals(3, events2.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events2.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events2.get(1).getClass());
        Assert.assertEquals(RunEvents.JVMDestroyed.class,events2.get(2).getClass());
        Assert.assertEquals(3,commandResponses2.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses2.get(0).getClass());
        Assert.assertEquals(CommandResponse.CommandResponseFailure.class,commandResponses2.get(1).getClass());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses2.get(2).getClass());


    }

    @Test
    public void parallelMissionsTest() throws Exception {

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

        Assert.assertEquals(3, events1.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events1.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events1.get(1).getClass());
        Assert.assertEquals(RunEvents.JVMDestroyed.class,events1.get(2).getClass());
        Assert.assertEquals(2,commandResponses1.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses1.get(0).getClass());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses1.get(1).getClass());


        Assert.assertEquals(3, events2.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events2.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events2.get(1).getClass());
        Assert.assertEquals(RunEvents.JVMDestroyed.class,events2.get(2).getClass());
        Assert.assertEquals(2,commandResponses2.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses2.get(0).getClass());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses2.get(1).getClass());

    }
}
