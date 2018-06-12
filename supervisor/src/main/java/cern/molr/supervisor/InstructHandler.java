package cern.molr.supervisor;

import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.web.DataExchangeBuilder;
import cern.molr.supervisor.impl.supervisor.MoleSupervisorService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

;

/**
 * WebSocket Spring Handler which handles websoscket requests for instructing the MoleRunner. It returns a mono, It
 * uses WebFlux
 *
 * @author yassine-kr
 */
@Component
public class InstructHandler implements WebSocketHandler {

    private final MoleSupervisorService supervisor;

    public InstructHandler(MoleSupervisorService supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        return session.send(new DataExchangeBuilder<>
                (MissionCommandRequest.class, CommandResponse.class)
                .setPreInput(session.receive().map(WebSocketMessage::getPayloadAsText))
                .setGenerator(supervisor::instruct)
                .setGeneratorExceptionHandler(CommandResponse.CommandResponseFailure::new)
                .build().map(session::textMessage));
    }
}
