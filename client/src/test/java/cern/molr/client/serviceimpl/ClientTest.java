package cern.molr.client.serviceimpl;

import cern.molr.mission.controller.ClientMissionController;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.commons.response.CommandResponse;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.sample.mission.Fibonacci;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for testing client Api.
 * Each test can fail if the thread finishes before getting all results from the supervisor, in that case the sleep duration should be increased
 *
 * @author yassine-kr
 */
public class ClientTest {

    /**
     * To execute this test, MolR Server must be started at port 8000 (it is the default port defined in the file "application.properties" of the module "server")
     * Supervisor Server must be started just after to be registered in MolR server
     */
    @Test
    public void MissionTest() throws Exception {

        List<MoleExecutionEvent> events=new ArrayList<>();
        List<MoleExecutionCommandResponse> commandResponses=new ArrayList<>();

        MissionExecutionService service=new MissionExecutionServiceImpl();
        Mono<ClientMissionController> futureController=service.instantiate(Fibonacci.class.getCanonicalName(),100);
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
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses.get(0).getClass());
        Assert.assertEquals(CommandResponse.CommandResponseSuccess.class,commandResponses.get(1).getClass());
    }
}
