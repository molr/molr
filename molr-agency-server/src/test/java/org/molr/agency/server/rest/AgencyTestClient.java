package org.molr.agency.server.rest;

import org.molr.commons.domain.dto.TestValueDto;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class AgencyTestClient {

    public static void main(String... args) throws InterruptedException {
        WebClient client = WebClient.create("http://localhost:8000");

        Mono<ClientResponse> getAllNames = client.get()
                .uri("/test-stream/5")
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange();
        getAllNames.subscribe(res -> res.bodyToFlux(TestValueDto.class).subscribe(v -> System.out.println(v)));


        Thread.sleep(20000);
    }
}

