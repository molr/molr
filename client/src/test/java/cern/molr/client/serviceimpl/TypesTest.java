package cern.molr.client.serviceimpl;

import cern.molr.commons.response.CommandResponse;
import cern.molr.exception.*;
import cern.molr.mission.controller.ClientMissionController;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.sample.mission.IncompatibleMission;
import cern.molr.sample.mission.NotAcceptedMission;
import cern.molr.sample.mission.RunnableExceptionMission;
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

/**
 * Class for testing object types returned by the server
 * Each test can fail if the thread finishes before getting all results from the supervisor,
 * in that case the sleep duration should be increased
 *
 * @author yassine-kr
 */
public class TypesTest {

    private ConfigurableApplicationContext contextServer;
    private ConfigurableApplicationContext contextSupervisor;
    private MissionExecutionService service=new MissionExecutionServiceImpl("localhost",8000);;


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
    public void commandResponseTest() throws Exception {

        List<MoleExecutionCommandResponse> commandResponses=new ArrayList<>();

        Mono<ClientMissionController> futureController=service.instantiate(Fibonacci.class.getName(),100);


        futureController.doOnError(Throwable::printStackTrace).subscribe((controller)->{
            controller.getFlux().subscribe((event)->{
                System.out.println("event: "+event);
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
            controller.instruct(new RunCommands.Start()).subscribe((response)->{
                System.out.println("response to start 2: "+response);
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

        Thread.sleep(5000);

        Assert.assertEquals(3,commandResponses.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses.get(0).getClass());
        Assert.assertEquals("command accepted by the JVM",
                ((CommandResponse.CommandResponseSuccess)commandResponses.get(0)).getResult().getMessage());
        Assert.assertEquals(CommandResponse.CommandResponseFailure.class,commandResponses.get(1).getClass());
        Assert.assertEquals(CommandNotAcceptedException.class,
                ((CommandResponse.CommandResponseFailure)commandResponses.get(1)).getThrowable().getClass());
        Assert.assertEquals("Command not accepted by the JVM: the mission is already started",
                ((CommandResponse.CommandResponseFailure)commandResponses.get(1)).getThrowable().getMessage());

    }

    @Test
    public void IncompatibleMissionTest() throws Exception {

        List<MoleExecutionEvent> events=new ArrayList<>();

        Mono<ClientMissionController> futureController=service.instantiate(IncompatibleMission.class.getName(),100);


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
        });

        Thread.sleep(5000);

        Assert.assertEquals(RunEvents.MissionException.class,events.get(0).getClass());
        Assert.assertEquals(MissionMaterializationException.class,
                ((RunEvents.MissionException)events.get(0)).getThrowable().getClass());
        Assert.assertEquals(IncompatibleMissionException.class,
                ((RunEvents.MissionException)events.get(0)).getThrowable().getCause().getClass());
        Assert.assertEquals("Mission must implement Runnable interface",
                ((RunEvents.MissionException)events.get(0)).getThrowable().getCause().getMessage());

    }


    @Test
    public void ExecutionExceptionTest() throws Exception {

        List<MoleExecutionEvent> events=new ArrayList<>();

        Mono<ClientMissionController> futureController=
                service.instantiate(RunnableExceptionMission.class.getName(),null);


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
            });
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            controller.instruct(new RunCommands.Terminate()).subscribe((response)->{
                System.out.println("response to terminate: "+response);
            });
        });

        Thread.sleep(5000);

        Assert.assertEquals(RunEvents.MissionException.class,events.get(2).getClass());
        Assert.assertEquals(MissionExecutionException.class,
                ((RunEvents.MissionException)events.get(2)).getThrowable().getClass());
        Assert.assertEquals(RuntimeException.class,
                ((RunEvents.MissionException)events.get(2)).getThrowable().getCause().getClass());

    }

    @Test
    public void NotAcceptedMissionTest() throws Exception {


        Mono<ClientMissionController> futureController=service.instantiate(NotAcceptedMission.class.getName(),0);

        final Throwable[] exception = new Throwable[1];

        futureController.doOnError((t)->{
            exception[0] =t;
        }).subscribe();

        Thread.sleep(5000);

        Assert.assertEquals(MissionExecutionNotAccepted.class, exception[0].getClass());
        Assert.assertEquals("Mission not defined in MolR registry", exception[0].getMessage());


    }



}
