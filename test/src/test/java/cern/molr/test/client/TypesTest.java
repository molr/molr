package cern.molr.test.client;

import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.client.impl.MissionExecutionServiceImpl;
import cern.molr.commons.commands.Start;
import cern.molr.commons.commands.Terminate;
import cern.molr.commons.events.MissionException;
import cern.molr.commons.events.MissionStarted;
import cern.molr.commons.events.SessionInstantiated;
import cern.molr.commons.exception.*;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionEvent;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.sample.mission.IncompatibleMission;
import cern.molr.sample.mission.NotAcceptedMission;
import cern.molr.sample.mission.RunnableExceptionMission;
import cern.molr.server.ServerMain;
import cern.molr.supervisor.RemoteSupervisorMain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Class for testing object types returned by the server
 *
 * @author yassine-kr
 */
public class TypesTest {

    private ConfigurableApplicationContext serverContext;
    private ConfigurableApplicationContext supervisorContext;
    private MissionExecutionService service=new MissionExecutionServiceImpl("localhost",8000);;


    @Before
    public void initServers(){
        serverContext =SpringApplication.run(ServerMain.class, new String[]{"--server.port=8000"});

        supervisorContext =SpringApplication.run(RemoteSupervisorMain.class,
                new String[]{"--server.port=8056","--molr.host=localhost","--molr.port=8000"});
    }

    @After
    public void exitServers(){
        SpringApplication.exit(supervisorContext);
        SpringApplication.exit(serverContext);
    }


    @Test
    public void commandResponseTest() throws Exception {

        CountDownLatch instantiateSignal = new CountDownLatch(1);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(6);

        List<CommandResponse> commandResponses=new ArrayList<>();

        Mono<ClientMissionController> futureController=service.instantiate(Fibonacci.class.getName(),100);


        futureController.doOnError(Throwable::printStackTrace).subscribe((controller)->{
            controller.getFlux().subscribe((event)->{
                System.out.println("event: "+event);
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
                Assert.fail();
            }
            controller.instruct(new Start()).subscribe((response)->{
                System.out.println("response to start: "+response);
                commandResponses.add(response);
                endSignal.countDown();
            });
            controller.instruct(new Start()).subscribe((response)->{
                System.out.println("response to start 2: "+response);
                commandResponses.add(response);
                endSignal.countDown();
            });
            try {
                startSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Assert.fail();
            }
            controller.instruct(new Terminate()).subscribe((response)->{
                System.out.println("response to terminate: "+response);
                commandResponses.add(response);
                endSignal.countDown();
            });
        });

        endSignal.await();

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

        CountDownLatch endSignal = new CountDownLatch(1);
        List<MissionEvent> events=new ArrayList<>();

        Mono<ClientMissionController> futureController=service.instantiate(IncompatibleMission.class.getName(),100);


        futureController.doOnError(Throwable::printStackTrace).subscribe((controller)->{
            controller.getFlux().subscribe((event)->{
                System.out.println("event: "+event);
                events.add(event);
                endSignal.countDown();
            });
        });

        endSignal.await();

        Assert.assertEquals(MissionException.class,events.get(0).getClass());
        Assert.assertEquals(MissionMaterializationException.class,
                ((MissionException)events.get(0)).getThrowable().getClass());
        Assert.assertEquals(IncompatibleMissionException.class,
                ((MissionException)events.get(0)).getThrowable().getCause().getClass());
        Assert.assertEquals("Mission must implement Runnable interface",
                ((MissionException)events.get(0)).getThrowable().getCause().getMessage());

    }


    @Test
    public void ExecutionExceptionTest() throws Exception {

        List<MissionEvent> events=new ArrayList<>();

        CountDownLatch instantiateSignal = new CountDownLatch(1);
        CountDownLatch startSignal = new CountDownLatch(1);
        CountDownLatch endSignal = new CountDownLatch(5);

        Mono<ClientMissionController> futureController=service.instantiate(RunnableExceptionMission.class
                .getCanonicalName(),null);
        futureController.doOnError(Throwable::printStackTrace).subscribe((controller)->{
            controller.getFlux().subscribe((event)->{
                System.out.println("event: "+event);
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
                Assert.fail();
            }
            controller.instruct(new Start()).subscribe((response)->{
                System.out.println("response to start: "+response);
                endSignal.countDown();
            });

            try {
                startSignal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Assert.fail();
            }
            controller.instruct(new Terminate()).subscribe((response)->{
                System.out.println("response to terminate: "+response);
                endSignal.countDown();
            });
        });

        endSignal.await();

        Assert.assertEquals(MissionException.class,events.get(2).getClass());
        Assert.assertEquals(MissionExecutionException.class,
                ((MissionException)events.get(2)).getThrowable().getClass());
        Assert.assertEquals(RuntimeException.class,
                ((MissionException)events.get(2)).getThrowable().getCause().getClass());

    }

    @Test
    public void NotAcceptedMissionTest() throws Exception {

        CountDownLatch endSignal = new CountDownLatch(1);

        Mono<ClientMissionController> futureController=service.instantiate(NotAcceptedMission.class.getName(),0);

        final Throwable[] exception = new Throwable[1];

        futureController.doOnError((t)->{
            exception[0] =t;
            endSignal.countDown();
        }).subscribe();

        endSignal.await();

        Assert.assertEquals(MissionExecutionNotAccepted.class, exception[0].getClass());
        Assert.assertEquals("Mission not defined in MolR registry", exception[0].getMessage());


    }



}
