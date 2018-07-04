package cern.molr.supervisor;

import cern.molr.commons.api.request.server.SupervisorHeartbeatRequest;
import cern.molr.commons.api.response.SupervisorState;
import cern.molr.commons.web.DataProcessorBuilder;
import cern.molr.supervisor.impl.supervisor.MoleSupervisorService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

;

/**
 * WebSocket Spring Handler which handles websoscket requests for getting the supervisor heartbeat. It returns a
 * flux as response. It uses WebFlux
 *
 * @author yassine-kr
 */
@Component
public class HeartbeatHandler implements WebSocketHandler {

    private final MoleSupervisorService supervisor;

    public HeartbeatHandler(MoleSupervisorService supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        return session.send(new DataProcessorBuilder<SupervisorHeartbeatRequest, SupervisorState>(SupervisorHeartbeatRequest
                .class)
                .setPreInput(session.receive().map(WebSocketMessage::getPayloadAsText))
                .setGenerator((request) -> supervisor.getHeartbeat(request.getInterval()))
                .setGeneratorExceptionHandler(null)
                .build().map(session::textMessage));
    }
}
