package cern.molr.commons.impl.web;

import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.request.server.InstantiationRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.web.MolrWebSocketClient;
import cern.molr.commons.web.MolrConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;

import java.io.IOException;
import java.net.URI;

/**
 * Client which is able to create WebSockets connections to a server using Spring WebFlux
 *
 * @author yassine-kr
 */
public class MolrWebSocketClientImpl implements MolrWebSocketClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MolrWebSocketClientImpl.class);

    private WebSocketClient client;
    private String host;
    private int port;

    public MolrWebSocketClientImpl(String host, int port) {
        client = new ReactorNettyWebSocketClient();
        this.host = host;
        this.port = port;
    }

    /**
     * Method which sends a request to sever ans receive a flux of data from it
     *
     * @param responseType
     * @param <T>
     *
     * @return try elements, if the serialization of an element throws an exception, this exception is pushed to flux
     */
    public <I, T> Flux<T> receiveFlux(String path, Class<T> responseType, I request) {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        /**
         * Processor which receives data from server
         */
        FluxProcessor<T, T> processor = TopicProcessor.create();

        client.execute(URI.create(host + ":" + port + path), session -> {
            try {
                return session.send(Mono.just(session.textMessage(mapper.writeValueAsString(request))))
                        .thenMany(session.receive().map((message) -> {
                            try {
                                return mapper.readValue(message.getPayloadAsText(), responseType);
                            } catch (IOException error) {
                                LOGGER.error("error while deserializing a received data", error);
                                return null;
                            }
                        })).doOnComplete(processor::onComplete).doOnNext(processor::onNext).then();
            } catch (JsonProcessingException error) {
                LOGGER.error("error while serializing the request", error);
                return Mono.error(error);
            }
        }).doOnError(processor::onError).subscribe();

        return processor;
    }

    /**
     * Method which sends a request to sever ans receive a single data from it
     *
     * @param path         the url path
     * @param responseType the response class
     * @param request      the request to send
     * @param <I>          the request type
     * @param <T>          the response type
     *
     * @return a stream of one element containing the a try element of the response type
     */
    public <I, T> Mono<T> receiveMono(String path, Class<T> responseType, I request) {
        return receiveFlux(path, responseType, request).next();
    }

    @Override
    public <I> Publisher<MissionEvent> instantiate(String missionName, String missionId, I missionArguments) {
        InstantiationRequest<I> request = new InstantiationRequest<>(missionId, missionName, missionArguments);
        return receiveFlux(MolrConfig.INSTANTIATE_PATH, MissionEvent.class, request)
                .doOnError((e) ->
                        LOGGER.error("error in instantiation stream [mission execution Id: {}, mission name: {}]",
                                missionId, missionName, e));
    }

    @Override
    public Publisher<MissionEvent> getEventsStream(String missionName, String missionId) {
        return receiveFlux(MolrConfig.EVENTS_STREAM_PATH, MissionEvent.class, missionId)
                .doOnError((e) ->
                        LOGGER.error("error in events stream [mission execution Id: {}, mission name: {}]", missionId,
                                missionName, e));
    }

    @Override
    public Publisher<CommandResponse> instruct(String missionName, String missionId, MissionCommand command) {
        MissionCommandRequest request = new MissionCommandRequest(missionId, command);
        return receiveMono(MolrConfig.INSTRUCT_PATH, CommandResponse.class, request)
                .doOnError((e) ->
                        LOGGER.error("error in command stream [mission execution Id: {}, mission name: {}, command: {}]",
                                missionId, missionName, command, e));
    }
}
