package cern.molr.supervisor;

import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.web.DataProcessorBuilder;
import cern.molr.supervisor.impl.supervisor.MoleSupervisorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 * WebSocket Spring Handler which handles websoscket requests for instructing the MoleRunner. It uses WebFlux.
 *
 * @author yassine-kr
 */
@Component
public class SupervisorInstructHandler implements WebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupervisorInstructHandler.class);

    private final MoleSupervisorService supervisor;

    public SupervisorInstructHandler(MoleSupervisorService supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        LOGGER.info("session created for a request received from the server: {}", session.getHandshakeInfo().getUri());

        return session.send(new DataProcessorBuilder<MissionCommandRequest, CommandResponse>(MissionCommandRequest.class)
                .setPreInput(session.receive().map(WebSocketMessage::getPayloadAsText))
                .setGenerator(supervisor::instruct)
                .setGeneratorExceptionHandler(CommandResponse::new)
                .build().map(session::textMessage));
    }
}
