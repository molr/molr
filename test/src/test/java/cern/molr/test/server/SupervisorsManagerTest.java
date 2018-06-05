package cern.molr.test.server;

import cern.molr.commons.request.MissionCommandRequest;
import cern.molr.commons.request.client.ServerInstantiationRequest;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionEvent;
import cern.molr.commons.response.SupervisorState;
import cern.molr.server.api.RemoteMoleSupervisor;
import cern.molr.server.api.SupervisorsManager;
import cern.molr.server.impl.SupervisorsManagerImpl;
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

    private SupervisorsManager manager = new SupervisorsManagerImpl();

    @Test
    public void Test() {

        RemoteMoleSupervisor s1 = new RemoteMoleSupervisorTest(true);
        RemoteMoleSupervisor s2 = new RemoteMoleSupervisorTest(true);
        RemoteMoleSupervisor s3 = new RemoteMoleSupervisorTest(false);
        RemoteMoleSupervisor s4 = new RemoteMoleSupervisorTest(true);

        String id1 = manager.addSupervisor(s1, Arrays.asList("A", "B", "C", "D"));
        String id2 = manager.addSupervisor(s2, Arrays.asList("A", "B", "D"));
        String id3 = manager.addSupervisor(s3, Arrays.asList("A", "C", "D"));
        String id4 = manager.addSupervisor(s4, Arrays.asList("A", "B", "C"));

        Optional<RemoteMoleSupervisor> optional = manager.chooseSupervisor("A");
        assertEquals(s1, optional.get());

        manager.removeSupervisor(s1);
        optional = manager.chooseSupervisor("A");
        assertEquals(s2, optional.get());

        manager.removeSupervisor(id2);
        optional = manager.chooseSupervisor("A");
        assertEquals(s4, optional.get());

        manager.removeSupervisor(s3);
        optional = manager.chooseSupervisor("D");
        Assert.assertFalse(optional.isPresent());

        optional = manager.chooseSupervisor("P");
        Assert.assertFalse(optional.isPresent());


    }

    public class RemoteMoleSupervisorTest implements RemoteMoleSupervisor {
        private SupervisorState supervisorState;

        public RemoteMoleSupervisorTest(boolean idle) {
            supervisorState = new SupervisorState(0, idle ? 1 : 0);
        }

        @Override
        public <I> Flux<MissionEvent> instantiate(ServerInstantiationRequest<I> request, String missionExecutionId) {
            return null;
        }

        @Override
        public Mono<CommandResponse> instruct(MissionCommandRequest command) {
            return null;
        }

        @Override
        public Optional<SupervisorState> getSupervisorState() {
            return Optional.of(supervisorState);
        }
    }

}