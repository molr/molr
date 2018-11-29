package org.molr.agency.remote.rest;

import org.molr.commons.domain.dto.MissionHandleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.Objects.requireNonNull;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

public class WebClientUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(WebClientUtils.class);

    private final WebClient client;

    private WebClientUtils(String baseUrl){
        requireNonNull(baseUrl, "baseUrl must not be null");
        client = WebClient.create(baseUrl);
    }

    public static WebClientUtils withBaseUrl(String baseUrl){
        return new WebClientUtils(baseUrl);
    }

    private static final void throwOnErrors(String uri, Mono<ClientResponse> clientResponse) {
        HttpStatus responseStatus = clientResponse.block().statusCode();
        if (responseStatus == HttpStatus.NOT_FOUND) {
            throw new IllegalStateException("Server response = NOT FOUND : may be a uri problem or wrong parameters. Uri was: '" + uri + "'.");
        } else if (responseStatus.isError()) {
            String errorMessage = clientResponse.block().bodyToMono(String.class).block();
            throw new IllegalArgumentException("error when calling " + uri
                    + " with http status " + responseStatus.name()
                    + " and error message: \n" + errorMessage);
        }
    }

    public <T> Flux<T> flux(String uri, Class<T> type) {
        Mono<ClientResponse> clientResponse = clientResponseForGet(uri, MediaType.APPLICATION_STREAM_JSON);
        throwOnErrors(uri, clientResponse);
        return clientResponse
                .doOnError(e -> LOGGER.error("Error while retrieving uri {}.", uri, e))
                .flatMapMany(response -> response.bodyToFlux(type));
    }

    public <T> Mono<T> mono(String uri, Class<T> type) {
        Mono<ClientResponse> clientResponse = clientResponseForGet(uri, MediaType.APPLICATION_JSON);
        throwOnErrors(uri, clientResponse);
        return clientResponse
                .doOnError(e -> LOGGER.error("Error while retrieving uri {}.", uri, e))
                .flatMap(response -> response.bodyToMono(type));
    }

    private Mono<ClientResponse> clientResponseForGet(String uri, MediaType mediaType) {
        return client.get()
                .uri(uri)
                .accept(mediaType)
                .exchange();
    }

    public void post(String uri, MediaType mediaType, BodyInserter<?, ? super ClientHttpRequest> body) {
        Mono<ClientResponse> response = client.post()
                .uri(uri)
                .accept(mediaType)
                .body(body)
                .exchange();
        throwOnErrors(uri, response);
        response.doOnError(e -> LOGGER.error("Posting request on URI {}.", uri, e)).subscribe();
    }

    public <T> Mono<T> postAndReturn(String uri, MediaType mediaType,Class<T> type, BodyInserter<?, ? super ClientHttpRequest> body) {
        Mono<ClientResponse> response = client.post()
                .uri(uri)
                .accept(mediaType)
                .body(body)
                .exchange();
        throwOnErrors(uri, response);
        return response.flatMap(res -> res.bodyToMono(type)).cache()
                .doOnError(e -> LOGGER.error("Posting request on URI {}.", uri, e));
    }
}
