package cern.molr.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration setting mapping between urls and websocket handlers
 *
 * @author yassine-kr
 */
@Configuration
public class WebSocketServerConfiguration {

    private final ServerRestExecutionService service;

    public WebSocketServerConfiguration(ServerRestExecutionService service) {
        this.service = service;
    }

    @Bean
    public HandlerMapping mapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/getFlux", new FluxServerHandler(service));
        map.put("/instruct", new InstructServerHandler(service));

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(1);
        mapping.setUrlMap(map);

        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
