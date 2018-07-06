package cern.molr.server;

import cern.molr.commons.api.response.MissionState;
import cern.molr.commons.web.DataProcessorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 * WebSocket Spring Handler which handles websoscket requests for getting the states stream concerning a mission
 * execution, it uses WebFlux.
 *
 * @author yassine-kr
 */
@Component
public class StatesStreamHandler implements WebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatesStreamHandler.class);

    private final ServerRestExecutionService service;

    public StatesStreamHandler(ServerRestExecutionService service) {
        this.service = service;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        LOGGER.info("session created for a request received from the client: {}", session.getHandshakeInfo().getUri());

        return session.send(new DataProcessorBuilder<String, MissionState>(String.class)
                .setPreInput(session.receive().map(WebSocketMessage::getPayloadAsText))
                .setGenerator(service::getStatesStream)
                .setGeneratorExceptionHandler(null)
                .build().map(session::textMessage));
    }
}
