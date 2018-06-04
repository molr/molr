package cern.molr.supervisor;

import cern.molr.supervisor.impl.supervisor.MoleSupervisorService;
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
public class WebSocketSupervisorConfiguration {

    private final MoleSupervisorService supervisor;

    public WebSocketSupervisorConfiguration(MoleSupervisorService supervisor) {
        this.supervisor = supervisor;
    }

    @Bean
    public HandlerMapping mapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/instantiate", new InstantiateSupervisorHandler(supervisor));
        map.put("/instruct", new InstructSupervisorHandler(supervisor));

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
