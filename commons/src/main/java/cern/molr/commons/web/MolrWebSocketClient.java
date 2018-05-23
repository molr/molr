package cern.molr.commons.web;

import cern.molr.mole.supervisor.MoleExecutionEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
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
 * @author yassine-kr-kr
 */
public class MolrWebSocketClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MolrWebSocketClient.class);

    private WebSocketClient client;
    private String host;
    private int port;

    public MolrWebSocketClient(String host,int port){
        client=new ReactorNettyWebSocketClient();
        this.host=host;
        this.port=port;
    }

    /**
     * Method which sends a request to sever ans receive a flux of data from it
     * TODO find a way to generate errors if an exception is thrown when deserializing events instead of null
     * @param responseType
     * @param <T>
     * @return
     */
    public <I,T> Flux<T> receiveFlux(String path,Class<T> responseType,I request){

        ObjectMapper mapper=new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        /**
         * Processor which which receives data from server
         * TODO choose the best implementation to use
         */
        FluxProcessor<T,T> processor=TopicProcessor.create();

        client.execute(URI.create("ws://"+host+":"+port+path),session -> {
            try {
                return session.send(Mono.just(session.textMessage(mapper.writeValueAsString(request))))
                        .thenMany(session.receive().map((message)->{
                            try {
                                return mapper.readValue(message.getPayloadAsText(),responseType);
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                            })).doOnNext(processor::onNext).then();
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }
        }).doOnError(processor::onError).subscribe();

        return processor;
    }

    /**
     * Method which sends a request to sever ans receive a single data from it
     * @param path
     * @param responseType
     * @param request
     * @param <I>
     * @param <T>
     * @return
     */
    public <I,T> Mono<T> receiveMono(String path,Class<T> responseType,I request){
        return receiveFlux(path,responseType,request).next();
    }
}
