/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.impl.web;

import cern.molr.commons.api.request.client.ServerInstantiationRequest;
import cern.molr.commons.api.request.server.SupervisorStateRequest;
import cern.molr.commons.api.request.supervisor.SupervisorRegisterRequest;
import cern.molr.commons.api.request.supervisor.SupervisorUnregisterRequest;
import cern.molr.commons.api.response.*;
import cern.molr.commons.api.web.MolrWebClient;
import cern.molr.commons.web.MolrConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * @author ?
 * @author yassine-kr
 */
public class MolrWebClientImpl implements MolrWebClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MolrWebClientImpl.class);

    private WebClient client;

    public MolrWebClientImpl(String host, int port) {
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

        FluxProcessor<O, O> processor = TopicProcessor.create();
        client.post().uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromPublisher(Mono.just(request), requestClass)).exchange()
                .flatMapMany(value -> value.bodyToMono(responseClass)).doOnNext(processor::onNext).doOnComplete
                (processor::onComplete).doOnError(processor::onError).subscribe();

        return processor.next();
    }

    @Override
    public <I, C> Publisher<C> instantiate(String missionName, I missionArguments, Function<String, C> mapper) {
        ServerInstantiationRequest<I> request = new ServerInstantiationRequest<>(missionName, missionArguments);
        return post(MolrConfig.INSTANTIATE_PATH, ServerInstantiationRequest.class, request, InstantiationResponse.class)
                .map(tryResponse -> tryResponse.match((Throwable e) -> {
                            throw new CompletionException(e);
                        },
                        InstantiationResponseBean::getMissionId))
                .map(mapper)
                .doOnError((e) -> LOGGER.error("error while sending an instantiation request [mission name: {}]",
                        missionName, e.getCause()));
    }

    @Override
    public Optional<SupervisorState> getState() {
        try {
            return post(MolrConfig.GET_STATE_PATH, SupervisorStateRequest.class, new SupervisorStateRequest(),
                    SupervisorStateResponse.class).block().match((throwable) -> {
                LOGGER.error("unable to get the supervisor state", throwable);
                return Optional.empty();
            }, Optional::<SupervisorState>ofNullable);
        } catch (Exception error) {
            LOGGER.error("unable to get the supervisor state", error);
            return Optional.empty();
        }
    }

    @Override
    public String register(String host, int port, List<String> acceptedMissions) {
        SupervisorRegisterRequest request = new SupervisorRegisterRequest(host, port, acceptedMissions);
        return post(MolrConfig.REGISTER_PATH, SupervisorRegisterRequest.class, request, SupervisorRegisterResponse
                .class).map((tryResponse) -> tryResponse.match(throwable -> {
                    throw new CompletionException(throwable);
                }
                , Function.identity())).block().getSupervisorId();
    }

    @Override
    public void unregister(String supervisorId) {
        SupervisorUnregisterRequest request = new SupervisorUnregisterRequest(supervisorId);
        post(MolrConfig.UNREGISTER_PATH, SupervisorUnregisterRequest.class, request, SupervisorUnregisterResponse.class)
                .block();
    }
}