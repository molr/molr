package cern.molr.supervisor;

import cern.molr.commons.request.MissionCommandRequest;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.web.DataExchangeBuilder;
import cern.molr.supervisor.impl.supervisor.MoleSupervisorService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WebSocket Spring Handler which handles websoscket requests for instructing the MoleRunner. It returns a mono
 *
 * @author yassine-kr
 */
@Component
public class InstructSupervisorHandler implements WebSocketHandler {

    private final MoleSupervisorService supervisor;

    public InstructSupervisorHandler(MoleSupervisorService supervisor) {
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
