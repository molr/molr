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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class for testing client Api.
 *
 * @author yassine-kr
 */
public class ClientTest {

    private ConfigurableApplicationContext contextServer;
    private ConfigurableApplicationContext contextSupervisor;
    private MissionExecutionService service=new MissionExecutionServiceImpl("localhost",8000);

    @Before
    public void initServers() {
        contextServer=SpringApplication.run(ServerMain.class, new String[]{"--server.port=8000"});

        contextSupervisor=SpringApplication.run(RemoteSupervisorMain.class,
                new String[]{"--server.port=8056","--molr.host=localhost","--molr.port=8000"});
    }

    @After
    public void exitServers(){
        SpringApplication.exit(contextServer);
        SpringApplication.exit(contextSupervisor);
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
            commandResponses,CountDownLatch finishSignal) throws Exception{

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
                Assert.fail();
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
                Assert.fail();
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
                Assert.fail();
            }

        }).start();



    }

    /**
     * The mission execution should be long enough to terminate the JVM before the mission is finished
     * @throws Exception
     */
    @Test
    public void missionTest() throws Exception {

        List<MoleExecutionEvent> events=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses=new ArrayList<>();
        CountDownLatch finishSignal = new CountDownLatch(1);

        launchMission("exec",Fibonacci.class,events,commandResponses,finishSignal);
        finishSignal.await();

        Assert.assertEquals(3, events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(RunEvents.JVMDestroyed.class,events.get(2).getClass());
        Assert.assertEquals(2,commandResponses.size());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses.get(0).getClass());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses.get(1).getClass());
    }

    /**
     * The mission execution should be long enough to terminate the JVM before the mission is finished
     * Testing a sequential mission execution
     * @throws Exception
     */
    @Test
    public void missionsTest() throws Exception {

        CountDownLatch finishSignal1 = new CountDownLatch(1);

        List<MoleExecutionEvent> events1=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses1=new ArrayList<>();

        launchMission("exec1",Fibonacci.class,events1,commandResponses1,finishSignal1);
        finishSignal1.await();

        List<MoleExecutionEvent> events2=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses2=new ArrayList<>();

        CountDownLatch instantiateSignal2 = new CountDownLatch(1);
        CountDownLatch startSignal2 = new CountDownLatch(1);
        CountDownLatch endSignal2 = new CountDownLatch(6);


        Mono<ClientMissionController> futureController2=
                service.instantiate(Fibonacci.class.getCanonicalName(),100);


        futureController2.doOnError(Throwable::printStackTrace).subscribe((controller)->{
            controller.getFlux().subscribe((event)->{
                System.out.println("exec2 event: "+event);
                events2.add(event);
                endSignal2.countDown();

                if (event instanceof RunEvents.JVMInstantiated)
                    instantiateSignal2.countDown();
                else if (event instanceof RunEvents.MissionStarted)
                    startSignal2.countDown();
            });
            try {
                instantiateSignal2.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Assert.fail();
            }
            controller.instruct(new RunCommands.Start()).subscribe((response)->{
                System.out.println("exec2 response to start: "+response);
                commandResponses2.add(response);
                endSignal2.countDown();
            });
            controller.instruct(new RunCommands.Start()).subscribe((response)->{
                System.out.println("exec2 response to start 2: "+response);
                commandResponses2.add(response);
                endSignal2.countDown();
            });
            try {
                startSignal2.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Assert.fail();
            }
            controller.instruct(new RunCommands.Terminate()).subscribe((response)->{
                System.out.println("exec2 response to terminate: "+response);
                commandResponses2.add(response);
                endSignal2.countDown();
            });
        });

        endSignal2.await();

        CountDownLatch finishSignal3 = new CountDownLatch(1);
        List<MoleExecutionEvent> events3=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses3=new ArrayList<>();


        launchMission("exec3",Fibonacci.class,events3,commandResponses3,finishSignal3);
        finishSignal3.await();

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

    /**
     * The mission execution should be long enough to terminate the JVM before the mission is finished
     * @throws Exception
     */
    @Test
    public void parallelMissionsTest() throws Exception {

        CountDownLatch finishSignal = new CountDownLatch(2);

        List<MoleExecutionEvent> events1=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses1=new ArrayList<>();

        List<MoleExecutionEvent> events2=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses2=new ArrayList<>();

        launchMission("exec1",Fibonacci.class,events1,commandResponses1,finishSignal);
        launchMission("exec2",Fibonacci.class,events2,commandResponses2,finishSignal);
        finishSignal.await();



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
