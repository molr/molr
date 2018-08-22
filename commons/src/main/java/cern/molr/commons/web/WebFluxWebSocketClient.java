package cern.molr.commons.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
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
public class WebFluxWebSocketClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebFluxWebSocketClient.class);

    private org.springframework.web.reactive.socket.client.WebSocketClient client;
    private String host;
    private int port;

    public WebFluxWebSocketClient(String host, int port) {
        client = new ReactorNettyWebSocketClient();
        this.host = host;
        this.port = port;
    }

    /**
     * Method which sends a request to sever and receives a {@link Flux} of data from it
     *
     * @param path         the url path
     * @param responseType the response {@link Class}
     * @param request      the request to send
     * @param <I>          the request type
     * @param <T>          the response type
     *
     * @return a stream of data sent by the server
     */
    public <I, T> Flux<T> receiveFlux(String path, Class<T> responseType, I request) {

        ObjectMapper mapper = SerializationUtils.getMapper();

        FluxProcessor<T, T> processor = TopicProcessor.create();

        client.execute(URI.create(host + ":" + port + path), session -> {
            try {
                return session.send(Mono.just(session.textMessage(mapper.writeValueAsString(request))))
                        .thenMany(session.receive().map((message) -> {
                            try {
                                return mapper.readValue(message.getPayloadAsText(), responseType);
                            } catch (IOException error) {
                                LOGGER.error("unable to deserialize a received data [{}]", message.getPayloadAsText(), error);
                                return null;
                            }
                        })).doOnComplete(processor::onComplete).doOnNext(processor::onNext).then();
            } catch (JsonProcessingException error) {
                LOGGER.error("unable to serialize a request [{}]", request, error);
                return Mono.error(error);
            }
        }).doOnError(processor::onError).subscribe();

        return processor;
    }

    /**
     * Method which sends a request to sever and receive a single data from it
     *
     * @param path         the url path
     * @param responseType the response {@link Class}
     * @param request      the request to send
     * @param <I>          the request type
     * @param <T>          the response type
     *
     * @return a stream of one element
     */
    public <I, T> Mono<T> receiveMono(String path, Class<T> responseType, I request) {
        return receiveFlux(path, responseType, request).next();
    }

}
