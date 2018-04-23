package cern.molr.server.supervisor;

import cern.molr.mission.Mission;
import cern.molr.mission.step.StepResult;
import cern.molr.server.StatefulMoleSupervisor;
import cern.molr.server.SupervisorsManager;
import cern.molr.type.Ack;
import cern.molr.type.either.Either;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;

/**
 * Class for testing {@link SupervisorsManagerImpl}
 *
 * @author yassine
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
        public <I, O> CompletableFuture<O> run(Mission m, I args, String missionExecutionId) {
            return null;
        }

        @Override
        public <I, O> CompletableFuture<Either<StepResult, O>> step(Mission m, I args, String missionExecutionId) {
            return null;
        }

        @Override
        public <I, O> CompletableFuture<O> resume(Mission m, I args, String missionExecutionId) {
            return null;
        }

        @Override
        public CompletableFuture<Ack> cancel(String missionExecutionId) {
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