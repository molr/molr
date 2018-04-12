package cern.molr.commons.trye;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Serializer which save real exception class name
 * @author yassine
 */
public class TryResponseFailureSerializer<T> extends JsonSerializer<TryResponseFailure<T>> {

    @Override
    public void serialize(TryResponseFailure<T> value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        value.getThrowable().initCause(new Throwable(value.getThrowable().getClass().getCanonicalName()));
        gen.writeObjectField("throwable",value.getThrowable());
        gen.writeEndObject();
    }
}
