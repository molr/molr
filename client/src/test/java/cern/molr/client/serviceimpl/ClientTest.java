package cern.molr.client.serviceimpl;

import cern.molr.mission.run.RunMissionControllerNew;
import cern.molr.mission.service.MissionExecutionServiceNew;
import cern.molr.mole.spawner.debug.ResponseCommand;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionResponseCommand;
import cern.molr.sample.mission.Fibonacci;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for testing client api
 *
 * Each test can fail if the thread finishes before getting all results from supervisor, in that case sleep duration should be increased
 *
 * @author yassine
 */
public class ClientTest {

    /**
     * To run this test MolR Server must be started at port 8000 (it is defined in file "application.properties" of the module "server")
     * Supervisor Server must be started after to be registered in MolR
     * @throws Exception
     */
    @Test
    public void MissionTest() throws Exception {

        List<MoleExecutionEvent> events=new ArrayList<>();
        List<MoleExecutionResponseCommand> commandResponses=new ArrayList<>();

        MissionExecutionServiceNew service=new MissionExecutionServiceImplNew();
        Mono<RunMissionControllerNew> futureController=service.instantiate(Fibonacci.class.getCanonicalName(),100);
        futureController.doOnError(Throwable::printStackTrace).subscribe((controller)->{
           controller.getFlux().subscribe((event)->{
               System.out.println("event: "+event);
               events.add(event);
           });
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

        Assert.assertEquals(2, events.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events.get(1).getClass());
        Assert.assertEquals(2,commandResponses.size());
        Assert.assertEquals(ResponseCommand.ResponseCommandSuccess.class,commandResponses.get(0).getClass());
        Assert.assertEquals(ResponseCommand.ResponseCommandSuccess.class,commandResponses.get(1).getClass());
    }

    /**
     * To run this test MolR Server must be started at port 8000 (it is defined in file "application.properties" of the module "server")
     * Supervisor Server must be started after to be registered in MolR
     * @throws Exception
     */
    @Test
    public void TwoMissionsTest() throws Exception {

        List<MoleExecutionEvent> events1=new ArrayList<>();
        List<MoleExecutionResponseCommand> commandResponses1=new ArrayList<>();

        List<MoleExecutionEvent> events2=new ArrayList<>();
        List<MoleExecutionResponseCommand> commandResponses2=new ArrayList<>();

        MissionExecutionServiceNew service=new MissionExecutionServiceImplNew();
        Mono<RunMissionControllerNew> futureController1=service.instantiate(Fibonacci.class.getCanonicalName(),100);

        futureController1.doOnError(Throwable::printStackTrace).subscribe((controller)->{
            controller.getFlux().subscribe((event)->{
                System.out.println("event(1): "+event);
                events1.add(event);
            });
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

        Mono<RunMissionControllerNew> futureController2=service.instantiate(Fibonacci.class.getCanonicalName(),100);


        futureController2.doOnError(Throwable::printStackTrace).subscribe((controller)->{
            controller.getFlux().subscribe((event)->{
                System.out.println("event(2): "+event);
                events2.add(event);
            });
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

        Thread.sleep(60000);


        /*
        Assert.assertEquals(2, events1.size());
        Assert.assertEquals(RunEvents.JVMInstantiated.class,events1.get(0).getClass());
        Assert.assertEquals(RunEvents.MissionStarted.class,events1.get(1).getClass());
        Assert.assertEquals(2,commandResponses1.size());
        Assert.assertEquals(ResponseCommand.ResponseCommandSuccess.class,commandResponses1.get(0).getClass());
        Assert.assertEquals(ResponseCommand.ResponseCommandSuccess.class,commandResponses1.get(1).getClass());


        Assert.assertEquals(1,events2.size());
        Assert.assertEquals(RunEvents.MissionException.class,events2.get(0).getClass());
        */

    }
}
