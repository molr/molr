package cern.molr.server.supervisor;

import cern.molr.mission.Mission;
import cern.molr.mole.supervisor.MissionCommandRequest;
import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.server.SupervisorsManager;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * Class for testing {@link SupervisorsManagerImpl}
 *
 * @author yassine-kr
 */
public class SupervisorsManagerTest {

    private SupervisorsManager manager=new SupervisorsManagerImpl();

    public class StatefulMoleSupervisorTest implements StatefulMoleSupervisor {
        private boolean idle;

        public StatefulMoleSupervisorTest(boolean idle) {
            this.idle = idle;
        }

        @Override
        public boolean isIdle() {
            return idle;
        }

        @Override
        public <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId) {
            return null;
        }

        @Override
        public Mono<MoleExecutionCommandResponse> instruct(MissionCommandRequest command) {
            return null;
        }
    }


    @Test
    public void Test() {
        StatefulMoleSupervisor s1=new StatefulMoleSupervisorTest(true);
        StatefulMoleSupervisor s2=new StatefulMoleSupervisorTest(true);
        StatefulMoleSupervisor s3=new StatefulMoleSupervisorTest(false);
        StatefulMoleSupervisor s4=new StatefulMoleSupervisorTest(true);

        String id1=manager.addSupervisor(s1,Arrays.asList("A","B","C","D"));
        String id2=manager.addSupervisor(s2,Arrays.asList("A","B","D"));
        String id3=manager.addSupervisor(s3,Arrays.asList("A","C","D"));
        String id4=manager.addSupervisor(s4,Arrays.asList("A","B","C"));

        Optional<StatefulMoleSupervisor> optional=manager.chooseSupervisor("A");
        assertEquals(s1,optional.get());

        manager.removeSupervisor(s1);
        optional=manager.chooseSupervisor("A");
        assertEquals(s2,optional.get());

        manager.removeSupervisor(id2);
        optional=manager.chooseSupervisor("A");
        assertEquals(s4,optional.get());

        manager.removeSupervisor(s3);
        optional=manager.chooseSupervisor("D");
        Assert.assertFalse(optional.isPresent());

        optional=manager.chooseSupervisor("P");
        Assert.assertFalse(optional.isPresent());

    }

}