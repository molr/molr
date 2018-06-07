package cern.molr.server;

import cern.molr.commons.request.MissionCommandRequest;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.web.DataExchangeBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * WebSocket Spring Handler which handles websoscket requests for instructing a mission execution
 *
 * @author yassine-kr
 */
@Component
public class InstructServerHandler implements WebSocketHandler {

    private final ServerRestExecutionService service;

    public InstructServerHandler(ServerRestExecutionService service) {
        this.service = service;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        return session.send(new DataExchangeBuilder<>
                (MissionCommandRequest.class, CommandResponse.class)
                .setPreInput(session.receive().map(WebSocketMessage::getPayloadAsText))
                .setGenerator((commandRequest) ->
                        Flux.from(service.instruct(commandRequest))
                )
                .setGeneratorExceptionHandler(CommandResponse.CommandResponseFailure::new)
                .build().map(session::textMessage));
    }
}
