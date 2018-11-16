package cern.molr.commons.web;

import cern.molr.commons.web.deserializers.InvocationExceptionDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.lang.reflect.InvocationTargetException;

/**
 * Class wrapping some utility functions concerning the serialization
 */
public abstract class SerializationUtils {
    private static ObjectMapper mapper = null;

    /**
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
