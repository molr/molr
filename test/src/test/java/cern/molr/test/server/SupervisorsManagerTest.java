package cern.molr.test.server;

import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.request.client.ServerInstantiationRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.SupervisorState;
import cern.molr.server.api.RemoteMoleSupervisor;
import cern.molr.server.api.SupervisorStateListener;
import cern.molr.server.api.SupervisorsManager;
import cern.molr.server.api.TimeOutStateListener;
import cern.molr.server.impl.SupervisorsManagerImpl;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.junit.Test;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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


        InvocationTargetException invocationTargetException = new InvocationTargetException(new Exception(), "jjj");
        Throwable runtimeException = new RuntimeException(new Exception());
        Throwable throwable = new Throwable(new Exception());
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(InvocationTargetException.class, new ThrowableDes());
        //module.addSerializer(Throwable.class, new ThrowableSerializer());
        mapper.registerModule(module);

        try {
            String jsonInvocation = mapper.writeValueAsString(invocationTargetException);
            String jsonThrowable= mapper.writeValueAsString(throwable);
            String jsonRuntime = mapper.writeValueAsString(runtimeException);
            System.out.println(jsonInvocation);
            System.out.println(jsonThrowable);
            System.out.println(jsonRuntime);
            throwable = mapper.readValue(jsonInvocation, Throwable.class);
            //runtimeException = mapper.readValue(jsonRuntime, Throwable.class);
            //System.out.println(throwable.getClass());
            //System.out.println(runtimeException.getClass());
            //invocationTargetException = mapper.readValue(jsonThrowable, InvocationTargetException.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public class ThrowableDes extends StdDeserializer<InvocationTargetException> {

        public ThrowableDes() {
            this(null);
        }

        public ThrowableDes(Class<?> vc) {
            super(vc);
        }

        @Override
        public InvocationTargetException deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            Throwable cause = null;
            StackTraceElement[] stackTraceElements = null;
            String message = null;

            for (jp.nextToken() ; jp.getCurrentToken() != JsonToken.END_OBJECT; jp.nextToken()) {
                jp.nextToken();

                switch (jp.getCurrentName()) {
                    case "cause":
                        try {
                            cause = (Throwable) ctxt.findRootValueDeserializer(SimpleType.constructUnsafe(Throwable
                                    .class)).deserialize(jp, ctxt);
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                        break;
                    case "stackTrace":
                        try {
                            stackTraceElements = (StackTraceElement[]) ctxt.findRootValueDeserializer(ArrayType.construct
                                    (SimpleType.constructUnsafe(StackTraceElement.class), null)).deserialize(jp, ctxt);
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                        break;
                    case "message":
                        try {
                            message = (String) ctxt.findRootValueDeserializer(SimpleType.constructUnsafe(String.class))
                                    .deserialize(jp, ctxt);
                        } catch (Exception error) {
                            error.printStackTrace();
                        }
                        default:
                            ctxt.findRootValueDeserializer(SimpleType.constructUnsafe(Object.class)).deserialize
                                    (jp, ctxt);
                }

            }

            System.out.println(cause);
            System.out.println(Arrays.toString(stackTraceElements));
            System.out.println(message);
            return new InvocationTargetException(new Exception());
        }
    }



    public class RemoteMoleSupervisorTest implements RemoteMoleSupervisor {
        private SupervisorState supervisorState;

        public RemoteMoleSupervisorTest(boolean idle) {
            supervisorState = new SupervisorState(0, idle ? 1 : 0);
        }

        @Override
        public <I> Publisher<MissionEvent> instantiate(ServerInstantiationRequest<I> request, String
                missionId) {
            return null;
        }

        @Override
        public Publisher<CommandResponse> instruct(MissionCommandRequest command) {
            return null;
        }

        @Override
        public Optional<SupervisorState> getSupervisorState() {
            return Optional.of(supervisorState);
        }

        @Override
        public void addStateListener(SupervisorStateListener listener) {

        }

        @Override
        public void addTimeOutStateListener(TimeOutStateListener listener) {

        }

        @Override
        public void close() {

        }
    }

}