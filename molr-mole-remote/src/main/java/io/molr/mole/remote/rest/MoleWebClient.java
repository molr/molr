package io.molr.mole.remote.rest;

import io.molr.commons.exception.MolrRemoteException;
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
import reactor.netty.http.client.PrematureCloseException;

import static io.molr.commons.util.Exceptions.exception;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

public class MoleWebClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(MoleWebClient.class);

    private final WebClient client;

    private MoleWebClient(String baseUrl) {
        requireNonNull(baseUrl, "baseUrl must not be null");
        client = WebClient.create(baseUrl);
    }

    public static MoleWebClient withBaseUrl(String baseUrl) {
        return new MoleWebClient(baseUrl);
    }

    public <T> Flux<T> flux(String uri, Class<T> type) {
        return clientResponseForGet(uri, TEXT_EVENT_STREAM)
                .flatMapMany(response -> response.bodyToFlux(type))
                .cache()
                .onErrorMap(throwable -> {
                    /*
                     * TODO consider not depending on netty exceptions
                     * however clients may need to detect disconnect events and pushing the dependency upwards would be even worse
                     * one alternative could be an offline flag located in a wrapper around items pushed through the flux
                     */
                	if(throwable instanceof PrematureCloseException) {
                		return new ConnectException("Connection prematurely closed "+uri);
                	}
                	else
                		return throwable;
                });
    }

    public <T> Mono<T> mono(String uri, Class<T> type) {
        return clientResponseForGet(uri, APPLICATION_JSON).flatMap(response -> response.bodyToMono(type));
    }

    public void post(String uri, MediaType mediaType, BodyInserter<?, ? super ClientHttpRequest> body) {
        clientResponseForPost(uri, mediaType, body);
    }

    public <T> Mono<T> postMono(String uri, MediaType mediaType, BodyInserter<?, ? super ClientHttpRequest> body, Class<T> type) {
        return clientResponseForPost(uri, mediaType, body).flatMap(r -> r.bodyToMono(type));
    }

    private Mono<ClientResponse> clientResponseForPost(String uri, MediaType mediaType, BodyInserter<?, ? super ClientHttpRequest> body) {
        Mono<ClientResponse> preparedRequest = client.post().uri(uri).accept(mediaType).body(body).exchange();
        return triggerRequest(uri, preparedRequest);
    }

    private Mono<ClientResponse> clientResponseForGet(String uri, MediaType mediaType) {
        Mono<ClientResponse> preparedRequest = client.get().uri(uri).accept(mediaType).exchange();
        return triggerRequest(uri, preparedRequest);
    }

    private static Mono<ClientResponse> triggerRequest(String uri, Mono<ClientResponse> preparedRequest) {
        /* caching will prevent the re-trigger of the http request */
    	//but retriggering might be useful in reconnect scenarios
        Mono<ClientResponse> cachedRequest = logAndFilterErrors(uri, preparedRequest).cache();
        /* subscribing here makes sure that the http call is initiated immediately */
        //Stefan what's the benefit?
        cachedRequest.subscribe();
        return cachedRequest;
    }

    private static Mono<ClientResponse> logAndFilterErrors(String uri, Mono<ClientResponse> clientResponse) {
        return clientResponse//
                .doOnNext(response -> logIfHttpErrorStatusCode(uri, response)) //
                .doOnNext(response -> {
                    if (!response.statusCode().is2xxSuccessful()) {
                        throw exception(MolrRemoteException.class, "Response from '{}' is not successful: {}", uri, response.statusCode());
                    }
                })
                .doOnError(e -> LOGGER.error("Error while retrieving uri {}.", uri, e))
                .onErrorMap(t -> {
                	return new ConnectException("Could not establish connection to "+uri);
                });
    }

    private static void logIfHttpErrorStatusCode(String uri, ClientResponse response) {
        HttpStatus responseStatus = response.statusCode();
        if (responseStatus == HttpStatus.NOT_FOUND) {
            LOGGER.error("Server response NOT FOUND when calling {}: uri problem or wrong parameters", uri);
        } else if (responseStatus.isError()) {
            LOGGER.error("Error when calling {} with http status {}", uri, responseStatus.name());
        }
    }

}
