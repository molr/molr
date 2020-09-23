package io.molr.mole.server.conf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.molr.commons.domain.MissionParameterDescription;
import io.molr.commons.domain.MissionParameter;

import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;


/**
 * @author krepp
 */
public class ParameterValueDeserializer extends StdDeserializer<Map<String, Object>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(ParameterValueDeserializer.class);
    
    private static final long serialVersionUID = 1L;

    private ObjectMapper mapper;
    
    MissionParameterDescription parameterDescription;

    /**
     * @param vc
     */
    protected ParameterValueDeserializer(Class<?> vc) {
        super(vc);
    }

    public ParameterValueDeserializer() {
        this(null);
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public static ParameterValueDeserializer with(ObjectMapper mapper, MissionParameterDescription parameterDescription) {
        ParameterValueDeserializer deserializer = new ParameterValueDeserializer();
        deserializer.setMapper(mapper);
        deserializer.setParameterDescription(parameterDescription);
        return deserializer;
    }

    private void setParameterDescription(MissionParameterDescription parameterDescription) {
        this.parameterDescription = parameterDescription;
    }

    @Override
    public Map<String, Object> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        
        JsonNode node = p.getCodec().readTree(p);
        Map<String, Object> params = new HashMap<>();        
        LOGGER.info("deserialize "+node);

        for (MissionParameter<?> describedParam : parameterDescription.parameters()) {

            String parameterName = describedParam.placeholder().name();
            JsonNode valueNode = node.get(parameterName);
            
            if (valueNode == null) {
                // TOOD need for actions in case of null?
                if (describedParam.isRequired()) {
                    throw new IllegalArgumentException(
                            "missing mandatory parameter " + describedParam.placeholder().name());
                }
            }
            else {
                try {
                    Object value = mapper.treeToValue(valueNode, describedParam.placeholder().type());
                    params.put(parameterName, value);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("Cannot deserialize parameter value for "+describedParam.placeholder());
                }
            }

        }
        
        return params;
        
    }
}
