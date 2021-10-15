package io.molr.mole.server.rest;

import static io.molr.commons.domain.MissionParameter.required;
import static io.molr.commons.domain.Placeholder.anInteger;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.util.Arrays;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.ListOfStrings;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionParameter;
import io.molr.commons.domain.MissionParameterDescription;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.dto.MissionParameterDescriptionDto;
import io.molr.commons.domain.dto.MissionParameterDto;
import io.molr.mole.core.api.Mole;
import io.molr.mole.server.conf.ObjectMapperConfig;
import reactor.core.publisher.Mono;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ContextConfiguration(classes = { MolrMoleRestService.class, ObjectMapperConfig.class })
@EnableAutoConfiguration
public class MolrMoleRestServiceTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(MolrMoleRestServiceTest.class);

    private final String baseUrl = "http://localhost:8800";

    @Autowired
    ExchangeStrategies exchangeStrategies;

    @MockBean
    Mole mole;

    @Test
    public void testTransportedMissionParametersSupportNullAsDefaultValue() {
        WebClient client = WebClient.create(baseUrl);
        String uri = "mission/aMission/parameterDescription";

        MissionParameter<Integer> parameter = required(anInteger("test-parameter"));
        MissionParameterDescription parameterDescription = new MissionParameterDescription(singleton(parameter));
        when(mole.parameterDescriptionOf(any(Mission.class))).thenReturn(Mono.just(parameterDescription));

        Mono<MissionParameterDescriptionDto> remoteParameters = client.get().uri(uri)
                .accept(MediaType.APPLICATION_NDJSON).exchangeToMono(c -> c.bodyToMono(MissionParameterDescriptionDto.class));

        MissionParameterDescriptionDto description = remoteParameters.block(Duration.ofSeconds(5));
        assertThat(description.parameters).hasSize(1);
        assertThat(description.parameters).as("it should have null default value")
                .anyMatch(param -> param.defaultValue == null);
    }

    @Test
    public void testTransportedMissionParametersSupportRetrieveExtendedParameterDescription() {

        WebClient client = WebClient.builder().exchangeStrategies(exchangeStrategies).baseUrl(baseUrl).build();
        String uri = "mission/aMission/parameterDescription";

        Placeholder.of(ListOfStrings.class, "anArrayListOfStrings");

        ListOfStrings defaultValue = new ListOfStrings(Lists.newArrayList("hello"));
        MissionParameter<ListOfStrings> stringListParam = required(Placeholder.aListOfStrings("stringArrayList"))
                .withDefault(defaultValue);

        MissionParameter<String[]> stringArrayParameter = required(
                Placeholder.of(String[].class, "stringArrayParameter")).withDefault(Arrays.array("A", "B"))
                        .withAllowed(ImmutableSet.of(Arrays.array("A", "B")));
        MissionParameter<String> stringParameter = required(Placeholder.of(String.class, "device"))
                .withDefault("hello");
        MissionParameter<Integer> intParameter = required(Placeholder.of(Integer.class, "anInt")).withDefault(1);
        MissionParameter<Long> longParameter = MissionParameter.optional(Placeholder.of(Long.class, "aLong"))
                .withDefault(5L);

        MissionParameterDescription parameterDescription = new MissionParameterDescription(
                ImmutableSet.of(stringArrayParameter, stringListParam, stringParameter, intParameter, longParameter));

        when(mole.parameterDescriptionOf(any(Mission.class))).thenReturn(Mono.just(parameterDescription));

        Mono<String> rawRemoteParameters = client.get().uri(uri).accept(MediaType.APPLICATION_NDJSON)
                .exchangeToMono(c -> c.bodyToMono(String.class));

        LOGGER.info("raw: " + rawRemoteParameters.block());

        Mono<MissionParameterDescriptionDto> remoteParameters = client.get().uri(uri)
                .accept(MediaType.APPLICATION_NDJSON)
                .exchangeToMono(c -> c.bodyToMono(MissionParameterDescriptionDto.class));

        MissionParameterDescriptionDto descriptionDto = remoteParameters.block();
        LOGGER.info("descriptionDto: " + descriptionDto.parameters);
        assertThat(descriptionDto.parameters).contains(new MissionParameterDto<>("stringArrayList",
                MissionParameterDto.TYPE_LIST_OF_STRINGS, true, defaultValue, new HashSet<>(), new HashMap<>()));
        descriptionDto.parameters.forEach(paramDto -> {
            LOGGER.info("\n" + paramDto.name);
            // LOGGER.info("defaultValueClass"+paramDto.defaultValue.getClass());
        });
        MissionParameterDescription retrievedDescription = descriptionDto.toMissionParameterDescription();
        retrievedDescription.parameters().forEach(param -> {
            LOGGER.info("parameter.placeholder " + param.placeholder());
            LOGGER.info("parameter.defaultValue: " + param.defaultValue());
            LOGGER.info(param.defaultValue().getClass().toString());
        });

    }

    @Test
    public void instantiateWithInvalidBody() {
        Set<String> params = new HashSet<>();
        WebClient client = WebClient.create(baseUrl);
        String uri = "mission/aMission/instantiate";

        HttpStatus response = client.post().uri(uri).accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(params)).retrieve().toBodilessEntity().map(ResponseEntity::getStatusCode)
                .block();
        assertThat(response).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testErrorIsThrownWhenMoleThrows() {
        String uri = "mission/aMission/representation";
        WebClient client = WebClient.create(baseUrl);
        when(mole.representationOf(any(Mission.class)))
                .thenThrow(new IllegalArgumentException("No mission of that name for this mole"));

        HttpStatus responseStatus = client.post().uri(uri).accept(MediaType.APPLICATION_JSON).retrieve()
                .toBodilessEntity().map(ResponseEntity::getStatusCode).block();

        assertThat(responseStatus).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void accessServerWithWrongUri() {
        WebClient client = WebClient.create(baseUrl);
        String uri = "mission/avail";
        HttpStatus response = client.post().uri(uri).accept(MediaType.APPLICATION_JSON).retrieve().toBodilessEntity()
                .map(ResponseEntity::getStatusCode).block();
        assertThat(response).isEqualTo(HttpStatus.NOT_FOUND);
    }

}
