package cern.molr.commons.web;

import cern.molr.commons.web.deserializers.InvocationExceptionDeserializer;
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Class wrapping some utility functions concerning the serialization
 */
public abstract class SerializationUtils {
    private static ObjectMapper mapper = null;

    /**
     *
     * @return the default {@link ObjectMapper} used for serialization and deserialization
     */
    public synchronized static ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            SimpleModule module = new SimpleModule();
            module.addDeserializer(InvocationTargetException.class, new InvocationExceptionDeserializer());
            mapper.registerModule(module);
        }
        return mapper;
    }

}
