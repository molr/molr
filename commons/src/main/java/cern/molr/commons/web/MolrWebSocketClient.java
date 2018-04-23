package cern.molr.commons.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * Client which is able to create WebSockets connections to a server using Spring WebFlux
 * TODO complete implementation of the class
 * @author yassine
 */
public class MolrWebSocketClient {

    private WebSocketClient client;
    private String host;
    private int port;
    private String path;

    public MolrWebSocketClient(String host,int port,String path){
        client=new ReactorNettyWebSocketClient();
        this.host=host;
        this.port=port;
        this.path=path;
    }

    /**
     * Method which receive a mono of a flux; mono represents task of connection to server, the flux is stream of data got from server
     * @param type
     * @param <T>
     * @return
     */
    public <T> Mono<Flux<T>> receiveFlux(Class<T> type){

        ObjectMapper mapper=new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return null;
        /*
        return Mono.create((emitter)->{
            client.execute(URI.create("ws://"+host+":"+port+path),session -> {
                WebSocketMessage::
                session.r
            });
        });
        */
    }
}
