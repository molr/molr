package io.molr.mole.server.conf;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.core.type.TypeReference;

import io.molr.commons.domain.dto.MissionParameterDto;
import io.molr.commons.domain.ListOfStrings;

 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;

/**
 * @author krepp
 */
public class MissionParameterDtoDeserializer extends StdDeserializer<MissionParameterDto<?>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(MissionParameterDtoDeserializer.class);
    
    private static final long serialVersionUID = 1L;

    private ObjectMapper mapper;

    public static final BiMap<Class<?>, String> TYPE_NAMES = HashBiMap.create();

    /**
     * @param vc
     */
    protected MissionParameterDtoDeserializer(Class<?> vc) {
        super(vc);
    }

    public MissionParameterDtoDeserializer() {
        this(null);
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public static MissionParameterDtoDeserializer with(ObjectMapper mapper) {
        MissionParameterDtoDeserializer deserializer = new MissionParameterDtoDeserializer();
        deserializer.setMapper(mapper);
        return deserializer;
    }

    @Override
    public MissionParameterDto<?> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);
        LOGGER.info("deserialize "+node);
        String name = node.get("name").asText();
        String type = node.get("type").asText();
        boolean required = node.get("required").asBoolean();
        JsonNode defaultValueNode = node.get("defaultValue");
        JsonNode allowedValuesNode = node.get("allowedValues");
        if (type.equals(MissionParameterDto.TYPE_LIST_OF_STRINGS)) {

            ListOfStrings defaultValue= mapper.treeToValue(defaultValueNode, ListOfStrings.class);
            TypeReference<Set<ListOfStrings>> typeReference = new TypeReference<Set<ListOfStrings>>() {
                //  
              };
            ObjectReader allowedValuesReader = mapper.readerFor(typeReference);
            Set<ListOfStrings> allowedValues = allowedValuesReader.readValue(allowedValuesNode);
            return new MissionParameterDto<>(name, type, required, defaultValue, allowedValues);
        }
        if(type.equals(MissionParameterDto.TYPE_STRING_ARRAY)) {
            TypeReference<String[]> typeReference = new TypeReference<String[]>() {
              //
            };
            ObjectReader stringArrayReader = mapper.readerFor(typeReference);
            String[] defaultValue = stringArrayReader.readValue(defaultValueNode);         

            Set<String[]> allowedValues = readAllowedValues(allowedValuesNode);//allowedValuesReader.readValue(allowedValuesNode);
            return new MissionParameterDto<>(name, type, required, defaultValue, allowedValues);            
        }
        if(type.equals(MissionParameterDto.TYPE_BOOLEAN)) {
            Boolean defaultValue = defaultValueNode.asBoolean();
            Set<Boolean> allowedValues = readAllowedValues(allowedValuesNode);
            return new MissionParameterDto<>(name, type, required, defaultValue, allowedValues);
        }
        if(type.equals(MissionParameterDto.TYPE_DOUBLE)) {
            Double defaultValue = defaultValueNode.asDouble();
            return new MissionParameterDto<>(name, type, required, defaultValue, ImmutableSet.of());
        }
        if(type.equals(MissionParameterDto.TYPE_STRING)) {
            String defaultValue = defaultValueNode.asText();
            Set<String> allowedValues = readAllowedValues(allowedValuesNode);            
            return new MissionParameterDto<>(name, type, required, defaultValue, allowedValues);
        }
        if(type.equals(MissionParameterDto.TYPE_INTEGER)) {
            Integer defaultValue = defaultValueNode.asInt();
            return new MissionParameterDto<>(name, type, required, defaultValue, ImmutableSet.of());
        }
        if(type.equals(MissionParameterDto.TYPE_LONG)) {
            Long defaultValue = defaultValueNode.asLong();
            return new MissionParameterDto<>(name, type, required, defaultValue, ImmutableSet.of());
        }
        
        Object defaultValue = mapper.treeToValue(defaultValueNode, MissionParameterDto.TYPE_NAMES.inverse().get(type));
        return new MissionParameterDto<>(name, type, required, defaultValue, ImmutableSet.of());

        //throw new IllegalStateException("Type cannot be deserialized "+type);
    }
    
    public <T> Set<T> readAllowedValues(JsonNode node) throws IOException{
        TypeReference<Set<T>> typeRef = new TypeReference<Set<T>>() {
            //
        };
        ObjectReader allowedValuesReader = mapper.readerFor(typeRef);
        return allowedValuesReader.readValue(node);
    }

    //TODO maybe useful
//  TypeReference<List<String>> typeReference = new TypeReference<List<String>>() {//
//  };
//  JsonNode allowedValuesNode = node.get("allowedValues");
//  ObjectReader listReader = mapper.readerFor(typeReference);
//  listReader.readValue(allowedValuesNode);
}
