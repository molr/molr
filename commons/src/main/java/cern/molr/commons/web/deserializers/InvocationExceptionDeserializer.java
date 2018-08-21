package cern.molr.commons.web.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.SimpleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * A custom deserializer for {@link InvocationTargetException} because the default deserialization fails
 */
public class InvocationExceptionDeserializer extends StdDeserializer<InvocationTargetException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvocationExceptionDeserializer.class);

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

        for (jp.nextToken() ; jp.getCurrentToken() != JsonToken.END_OBJECT; jp.nextToken()) {
            jp.nextToken();

            switch (jp.getCurrentName()) {
                case "cause":
                    try {
                        cause = (Throwable) ctxt.findRootValueDeserializer(SimpleType.constructUnsafe(Throwable
                                .class)).deserialize(jp, ctxt);
                    } catch (Exception error) {
                        LOGGER.warn("error while trying to deserialize the cause field, null will be used", error);
                    }
                    break;
                case "stackTrace":
                    try {
                        stackTraceElements = (StackTraceElement[]) ctxt.findRootValueDeserializer(ArrayType.construct
                                (SimpleType.constructUnsafe(StackTraceElement.class), null)).deserialize(jp, ctxt);
                    } catch (Exception error) {
                        LOGGER.warn("error while trying to deserialize the stackTrace field, null will be used", error);
                    }
                    break;
                case "message":
                    try {
                        message = (String) ctxt.findRootValueDeserializer(SimpleType.constructUnsafe(String.class))
                                .deserialize(jp, ctxt);
                    } catch (Exception error) {
                        LOGGER.warn("error while trying to deserialize the message field, null will be used", error);
                    }
                default:
                    ctxt.findRootValueDeserializer(SimpleType.constructUnsafe(Object.class)).deserialize
                            (jp, ctxt);
            }

        }

        InvocationTargetException exception = new InvocationTargetException(cause, message);
        exception.setStackTrace(stackTraceElements);
        return exception;
    }
}
