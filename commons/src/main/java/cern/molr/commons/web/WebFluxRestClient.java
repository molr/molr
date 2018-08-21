/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;

/**
 * A client which can perform HTTP POST request using Spring WebFlux
 *
 * @author yassine-kr
 */
public class WebFluxRestClient {

    private WebClient client;

    /**
     * It creates the client which uses a specific {@link ObjectMapper} for serializing and deserializing
     */
    public WebFluxRestClient(String host, int port) {

        ExchangeStrategies strategies = ExchangeStrategies
                .builder()
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs()
                            .jackson2JsonEncoder(new Jackson2JsonEncoder(SerializationUtils.getMapper(), MediaType
                                    .APPLICATION_JSON));
                    clientDefaultCodecsConfigurer.defaultCodecs()
                            .jackson2JsonDecoder(new Jackson2JsonDecoder(SerializationUtils.getMapper(), MediaType.APPLICATION_JSON));
                }).build();
        this.client = WebClient.builder().baseUrl(host + ":" + port).exchangeStrategies(strategies)
                .build();
    }

    public <I, O> Mono<O> post(String uri, Class<I> requestClass, I request, Class<O> responseClass) {

        FluxProcessor<O, O> processor = TopicProcessor.create();
        client.post().uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(request), requestClass)).exchange()
                .flatMapMany(value -> value.bodyToMono(responseClass)).doOnNext(processor::onNext).doOnComplete
                (processor::onComplete).doOnError(processor::onError).subscribe();

        return processor.next();
    }

}