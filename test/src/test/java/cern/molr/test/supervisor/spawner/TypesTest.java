package cern.molr.test.supervisor.spawner;

import cern.molr.commons.api.exception.IncompatibleMissionException;
import cern.molr.commons.api.mission.Mission;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.commons.events.MissionExceptionEvent;
import cern.molr.commons.events.MissionStateEvent;
import cern.molr.commons.impl.mission.MissionImpl;
import cern.molr.sample.mole.RunnableMole;
import cern.molr.supervisor.api.session.MissionSession;
import cern.molr.supervisor.api.session.MoleController;
import cern.molr.supervisor.impl.spawner.JVMSpawner;
import cern.molr.test.MissionTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * class for testing object types returned by the MoleRunner
 *
 * @author yassine-kr
 */
public class TypesTest {


    @Test
    public void IncompatibleMissionTest() throws Exception {
        CountDownLatch signal = new CountDownLatch(3);

        JVMSpawner<Integer> spawner = new JVMSpawner<>();
        Mission mission = new MissionImpl(RunnableMole.class.getName(), MissionTest.class.getName());
        MissionSession session = spawner.spawnMoleRunner(mission, 100);
        MoleController controller = session.getController();

        List<MissionEvent> events = new ArrayList<>();

        controller.addMoleExecutionListener(event -> {
            if (event instanceof MissionStateEvent) {
                return;
            }
            System.out.println(event);
            events.add(event);
            signal.countDown();

        });
        controller.sendCommand(new MissionControlCommand(MissionControlCommand.Command.START));

        signal.await(1, TimeUnit.MINUTES);

        Assert.assertEquals(MissionExceptionEvent.class, events.get(1).getClass());
        Assert.assertEquals(IncompatibleMissionException.class,
                ((MissionExceptionEvent) events.get(1)).getThrowable().getClass());
        Assert.assertEquals("Mission must implement Runnable interface",
                ((MissionExceptionEvent) events.get(1)).getThrowable().getMessage());
    }


}