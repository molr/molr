/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class TryResponseDeserializer<T> extends JsonDeserializer<T> {

    public abstract Class<? extends T> getSuccessDeserializer();
    
    public abstract Class<? extends T> getFailureDeserializer();
    
    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode root = (ObjectNode) mapper.readTree(jp);
        Class<? extends T> instanceClass = null;
        if(root.get("exception").isNull()) {
            instanceClass = getSuccessDeserializer();
        } else { 
            instanceClass = getFailureDeserializer();
        }
        return mapper.readValue(root.toString(), instanceClass);
    }

    
}
