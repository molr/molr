package cern.molr.server;

import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.web.DataProcessorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 * WebSocket Spring Handler which handles websoscket requests for instructing a mission execution, it uses WebFlux.
 *
 * @author yassine-kr
 */
@Component
public class InstructHandler implements WebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstructHandler.class);

    private final ServerExecutionService service;

    public InstructHandler(ServerExecutionService service) {
        this.service = service;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        LOGGER.info("session created for a request received from the client: {}", session.getHandshakeInfo().getUri());

        return session.send(new DataProcessorBuilder<MissionCommandRequest, CommandResponse>(MissionCommandRequest.class)
                .setPreInput(session.receive().map(WebSocketMessage::getPayloadAsText))
                .setGenerator(service::instruct)
                .setGeneratorExceptionHandler(CommandResponse::new)
                .build().map(session::textMessage));
    }
}
