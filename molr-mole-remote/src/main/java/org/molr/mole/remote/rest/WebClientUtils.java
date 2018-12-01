package org.molr.mole.remote.rest;

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

public class WebClientUtils {

	private final static Logger LOGGER = LoggerFactory.getLogger(WebClientUtils.class);

	private final WebClient client;

	private WebClientUtils(String baseUrl) {
		requireNonNull(baseUrl, "baseUrl must not be null");
		client = WebClient.create(baseUrl);
	}

	public static WebClientUtils withBaseUrl(String baseUrl) {
		return new WebClientUtils(baseUrl);
	}

	private static final Mono<ClientResponse> logErrors(String uri, Mono<ClientResponse> clientResponse) {
		return clientResponse.doOnNext(response -> {
			HttpStatus responseStatus = response.statusCode();
			if (responseStatus == HttpStatus.NOT_FOUND) {
				LOGGER.error("Server response = NOT FOUND : uri problem or wrong parameters. Uri: '" + uri + "'.");
			} else if (responseStatus.isError()) {
				String errorMessage = response.bodyToMono(String.class).block();
				LOGGER.error("error when calling " + uri + " with http status " + responseStatus.name()
						+ " and error message: \n" + errorMessage);
			}
		}).doOnError(e -> LOGGER.error("Error while retrieving uri {}.", uri, e));
	}

	public <T> Flux<T> flux(String uri, Class<T> type) {
		Mono<ClientResponse> clientResponse = clientResponseForGet(uri, MediaType.APPLICATION_STREAM_JSON);
		return logErrors(uri, clientResponse).flatMapMany(response -> response.bodyToFlux(type));
	}

	public <T> Mono<T> mono(String uri, Class<T> type) {
		Mono<ClientResponse> clientResponse = clientResponseForGet(uri, MediaType.APPLICATION_JSON);
		return logErrors(uri, clientResponse).flatMap(response -> response.bodyToMono(type));
	}

	private Mono<ClientResponse> clientResponseForGet(String uri, MediaType mediaType) {
		return client.get().uri(uri).accept(mediaType).exchange();
	}

	public void post(String uri, MediaType mediaType, BodyInserter<?, ? super ClientHttpRequest> body) {
		Mono<ClientResponse> response = client.post().uri(uri).accept(mediaType).body(body).exchange();
		logErrors(uri, response).subscribe();
	}
}
