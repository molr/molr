package cern.molr.supervisor;

import cern.molr.commons.api.request.server.SupervisorHeartbeatRequest;
import cern.molr.commons.api.response.SupervisorState;
import cern.molr.commons.web.DataProcessorBuilder;
import cern.molr.supervisor.impl.supervisor.MoleSupervisorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

;

/**
 * WebSocket Spring Handler which handles websoscket requests for getting the supervisor heartbeat. It uses WebFlux.
 *
 * @author yassine-kr
 */
@Component
public class HeartbeatHandler implements WebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatHandler.class);

    private final MoleSupervisorService supervisor;

    public HeartbeatHandler(MoleSupervisorService supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        LOGGER.info("session created for a request received from the server: {}", session.getHandshakeInfo().getUri());

        return session.send(new DataProcessorBuilder<SupervisorHeartbeatRequest, SupervisorState>(SupervisorHeartbeatRequest
                .class)
                .setPreInput(session.receive().map(WebSocketMessage::getPayloadAsText))
                .setGenerator((request) -> supervisor.getHeartbeat(request.getInterval()))
                .setGeneratorExceptionHandler(null)
                .build().map(session::textMessage));
    }
}
