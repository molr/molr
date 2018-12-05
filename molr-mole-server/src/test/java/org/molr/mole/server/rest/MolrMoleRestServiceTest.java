package org.molr.mole.server.rest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.molr.commons.api.Agent;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionParameter;
import org.molr.commons.domain.MissionParameterDescription;
import org.molr.commons.domain.dto.MissionParameterDescriptionDto;
import org.molr.commons.domain.dto.MissionRepresentationDto;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.molr.commons.domain.MissionParameter.required;
import static org.molr.commons.domain.Placeholder.anInteger;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ContextConfiguration(classes = MolrMoleRestService.class)
@EnableAutoConfiguration
public class MolrMoleRestServiceTest {


    private final String baseUrl = "http://localhost:8800";

    @MockBean
    Agent mole;

    @Test
    public void testTransportedMissionParametersSupportNullOnDefaultValue() {
        WebClient client = WebClient.create(baseUrl);
        String uri = "mission/aMission/parameterDescription";

        MissionParameter<Integer> parameter = required(anInteger("test-parameter"));
        MissionParameterDescription parameterDescription = new MissionParameterDescription(singleton(parameter));
        when(mole.parameterDescriptionOf(any(Mission.class))).thenReturn(Mono.just(parameterDescription).cache());

        Mono<MissionParameterDescriptionDto> remoteParameters = client.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMap(c -> c.bodyToMono(MissionParameterDescriptionDto.class));

        MissionParameterDescriptionDto description = remoteParameters.block();
        assertThat(description.parameters).hasSize(1);
        assertThat(description.parameters).as("it should have null default value").anyMatch(param -> param.defaultValue == null);
    }

    @Test
    public void instantiateWithInvalidBody() {
        Set<String> params = new HashSet<>();
        WebClient client = WebClient.create(baseUrl);
        String uri = "mission/aMission/instantiate/aHandle";

        HttpStatus response = client.post()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(params))
                .exchange()
                .map(ClientResponse::statusCode).block();
        assertThat(response).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testErrorIsThrownWhenMoleThrows() {
        String uri = "mission/aMission/representation";
        MissionRepresentationDto representation;
        WebClient client = WebClient.create(baseUrl);
        when(mole.representationOf(any(Mission.class))).thenThrow(new IllegalArgumentException("No mission of that name for this mole"));

        HttpStatus responseStatus = client.get().uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .map(ClientResponse::statusCode).block();

        assertThat(responseStatus).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void accessServerWithWrongUri() {
        WebClient client = WebClient.create(baseUrl);
        String uri = "mission/avail";
        HttpStatus response = client.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .map(ClientResponse::statusCode).block();
        assertThat(response).isEqualTo(HttpStatus.NOT_FOUND);
    }


}