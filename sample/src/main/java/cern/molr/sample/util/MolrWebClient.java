/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.sample.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class MolrWebClient {

    private WebClient client;

    public MolrWebClient(String host, int port) {
        this.client = WebClient.create("http://"+host+":"+port);
    }

    public <I,O> CompletableFuture<O> post(String uri, Class<I> requestClass, I request, Class<O> responseClass) {
        return CompletableFuture.supplyAsync(
                () -> client.post().uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(request), requestClass)).exchange()
                .flatMapMany(value -> value.bodyToMono(responseClass))
                .doOnError(e -> {throw new CompletionException(e);})
                .blockFirst());
    }

}
