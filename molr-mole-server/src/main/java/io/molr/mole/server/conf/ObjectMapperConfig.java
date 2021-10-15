package io.molr.mole.server.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.molr.commons.domain.dto.MissionParameterDto;

/**
 *
 * @author krepp
 */
//@Configuration
public class ObjectMapperConfig {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectMapperConfig.class);
    
    @Bean
    public ObjectMapper objectMapper(){
        return createObjectMapper();
    }
    
    @Bean
    public Jackson2JsonDecoder jsonDecoder(ObjectMapper mapper) {
        return jsonDecoderWith(mapper);
    }
    
    private static Jackson2JsonDecoder jsonDecoderWith(ObjectMapper mapper) {
        Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(mapper);
        return decoder;
    }
    
    @Bean
    public Jackson2JsonEncoder jsonEncoder(ObjectMapper mapper) {
        return jsonEncoderWithMapper(mapper);
    }
    
    private static Jackson2JsonEncoder jsonEncoderWithMapper(ObjectMapper mapper) {
        Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(mapper);
        return encoder;
    }
    
    @Bean 
    public ExchangeStrategies exchangeStrategies(ObjectMapper mapper) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder().codecs(clientCodecConfigurer -> {
            Jackson2JsonDecoder decoder = new Jackson2JsonDecoder(mapper);
            Jackson2JsonEncoder encoder = new Jackson2JsonEncoder(mapper);
           clientCodecConfigurer.defaultCodecs().jackson2JsonDecoder(decoder);
           clientCodecConfigurer.defaultCodecs().jackson2JsonEncoder(encoder);
        }).build();
        return exchangeStrategies;
    }

    public static ExchangeStrategies createExchangeStrategies() {
        ObjectMapper mapper = createObjectMapper();
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder().codecs(clientCodecConfigurer -> {
            Jackson2JsonDecoder decoder = jsonDecoderWith(mapper);
            Jackson2JsonEncoder encoder = jsonEncoderWithMapper(mapper);
           clientCodecConfigurer.defaultCodecs().jackson2JsonDecoder(decoder);
           clientCodecConfigurer.defaultCodecs().jackson2JsonEncoder(encoder);
        }).build();
        return exchangeStrategies;
    }
    
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(MissionParameterDto.class, MissionParameterDtoDeserializer.with(mapper));
        mapper.registerModule(module);
        LOGGER.info("Registered custom deserializer "+MissionParameterDtoDeserializer.class);
        return mapper;
    }

    /*
    @Bean
    public WebFluxConfigurer configurer(Jackson2JsonDecoder decoder, Jackson2JsonEncoder encoder) {
        return new WebFluxConfigurer() {
            
            @Override
            public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
                configurer.defaultCodecs().jackson2JsonDecoder(decoder);
                configurer.defaultCodecs().jackson2JsonEncoder(encoder);
            }
            
        };
    }
    */

}

