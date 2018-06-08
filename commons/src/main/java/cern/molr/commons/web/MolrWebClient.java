/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.web;

import cern.molr.commons.request.client.ServerInstantiationRequest;
import cern.molr.commons.request.server.SupervisorStateRequest;
import cern.molr.commons.response.InstantiationResponse;
import cern.molr.commons.response.SupervisorState;
import cern.molr.commons.response.SupervisorStateResponse;
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
 * @author ?
 * @author yassine-kr
 */
public class MolrWebClient {

    private WebClient client;

    public MolrWebClient(String host, int port) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        ExchangeStrategies strategies = ExchangeStrategies
                .builder()
                .codecs(clientDefaultCodecsConfigurer -> {
                    clientDefaultCodecsConfigurer.defaultCodecs()
                            .jackson2JsonEncoder(new Jackson2JsonEncoder(mapper, MediaType.APPLICATION_JSON));
                    clientDefaultCodecsConfigurer.defaultCodecs()
                            .jackson2JsonDecoder(new Jackson2JsonDecoder(mapper, MediaType.APPLICATION_JSON));
                }).build();
        this.client = WebClient.builder().baseUrl(host + ":" + port).exchangeStrategies(strategies)
                .build();
    }

    public <I, O> Mono<O> post(String uri, Class<I> requestClass, I request, Class<O> responseClass) {

        FluxProcessor<O, O> processor=TopicProcessor.create();
        client.post().uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromPublisher(Mono.just(request), requestClass)).exchange()
                    .flatMapMany(value -> value.bodyToMono(responseClass)).doOnNext(processor::onNext).doOnComplete
                (processor::onComplete).doOnError(processor::onError).subscribe();

        return processor.next();
    }


    public <I> Mono<InstantiationResponse> instantiate(String missionName, I args) {
        ServerInstantiationRequest<I> request = new ServerInstantiationRequest<>(missionName, args);
        return post(MolrConfig.INSTANTIATE_PATH,ServerInstantiationRequest.class, request, InstantiationResponse.class);
    }

    public Mono<SupervisorStateResponse> getState() {
        return post(MolrConfig.GET_STATE_PATH,SupervisorStateRequest.class, new SupervisorStateRequest(), SupervisorStateResponse.class);

    }
}