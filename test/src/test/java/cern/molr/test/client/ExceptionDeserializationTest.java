package cern.molr.test.client;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Class for testing the deserialization of some exceptions
 */
public class ExceptionDeserializationTest {

    @Test
    public void invocationExceptionTypeTest() {
        InvocationTargetException invocationTargetException = new InvocationTargetException(new Exception
                ("innerMessage"), "message");

        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(InvocationTargetException.class, new InvocationExceptionDeserializer());
        mapper.registerModule(module);

        try {
            String jsonInvocation = mapper.writeValueAsString(invocationTargetException);
            System.out.println(jsonInvocation);
            Throwable throwable = mapper.readValue(jsonInvocation, Throwable.class);
            Assert.assertEquals(InvocationTargetException.class, throwable.getClass());
            Assert.assertEquals(Exception.class, throwable.getCause().getClass());
            Assert.assertEquals("innerMessage", throwable.getCause().getMessage());
            Assert.assertEquals("message", throwable.getMessage());
            Assert.assertNotNull(throwable.getStackTrace());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

    }


    @Test
    public void invocationExceptionTypelessTest() {
        InvocationTargetException invocationTargetException = new InvocationTargetException(new Exception
                ("innerMessage"), "message");

        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addDeserializer(InvocationTargetException.class, new InvocationExceptionDeserializer());
        mapper.registerModule(module);

        try {
            String jsonInvocation = mapper.writeValueAsString(invocationTargetException);
            System.out.println(jsonInvocation);
            Throwable throwable = mapper.readValue(jsonInvocation, InvocationTargetException.class);
            Assert.assertEquals(InvocationTargetException.class, throwable.getClass());
            Assert.assertEquals(Throwable.class, throwable.getCause().getClass());
            Assert.assertEquals("innerMessage", throwable.getCause().getMessage());
            Assert.assertEquals("message", throwable.getMessage());
            Assert.assertNotNull(throwable.getStackTrace());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            Assert.fail();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    public class InvocationExceptionDeserializer extends StdDeserializer<InvocationTargetException> {

        public InvocationExceptionDeserializer() {
            this(null);
        }

        public InvocationExceptionDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public InvocationTargetException deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            Throwable cause = null;
            StackTraceElement[] stackTraceElements = null;
            String message = null;

            for (jp.nextToken(); jp.getCurrentToken() != JsonToken.END_OBJECT; jp.nextToken()) {
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
            InvocationTargetException exception = new InvocationTargetException(cause, message);
            return exception;
        }
    }
}
