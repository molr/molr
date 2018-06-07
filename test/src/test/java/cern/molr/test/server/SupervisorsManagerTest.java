package cern.molr.test.server;

import cern.molr.client.impl.MissionExecutionServiceImpl;
import cern.molr.commons.request.MissionCommandRequest;
import cern.molr.commons.request.client.ServerInstantiationRequest;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionEvent;
import cern.molr.commons.response.SupervisorState;
import cern.molr.server.api.RemoteMoleSupervisor;
import cern.molr.server.api.SupervisorsManager;
import cern.molr.server.impl.SupervisorsManagerImpl;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Serializable;
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

        /*
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
        */

        ObjectMapper mapper=new ObjectMapper();

        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);


        Serializable r=new TestO(50);
        try {
            String seri=mapper.writeValueAsString(r);
            System.out.println(seri);
            TestO r2=(TestO) mapper.readValue(seri,TestO.class);
            System.out.println(r2.getA());
            System.out.println(r2.getT());
        } catch (JsonProcessingException error) {
            error.printStackTrace();
        } catch (IOException error) {
            error.printStackTrace();
        }

        Logger LOGGER = LoggerFactory.getLogger(MissionExecutionServiceImpl.class);
        LOGGER.error("error while receiving events flux [mission execution " +
                        "Id: {}, mission name: {}]", "juj",
                "deded", new Exception());

    }

    public static final class TestO implements Serializable {
        private int a;
        private String s;
        private final Throwable t;

        public TestO(@JsonProperty("a") int a) {
            this.a = a;
            this.s = s;
            t=new Exception("kokok");
        }

        public int getA() {
            return a;
        }

        public Throwable getT() {
            return t;
        }
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