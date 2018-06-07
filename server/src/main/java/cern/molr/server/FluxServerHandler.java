package cern.molr.server;

import cern.molr.commons.events.MissionException;
import cern.molr.commons.request.client.MissionEventsRequest;
import cern.molr.commons.response.MissionEvent;
import cern.molr.commons.web.DataExchangeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 * WebSocket Spring Handler which handles websoscket requests for getting flux of events concerning a mission execution
 *
 * @author yassine-kr
 */
@Component
public class FluxServerHandler implements WebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FluxServerHandler.class);

    private final ServerRestExecutionService service;

    public FluxServerHandler(ServerRestExecutionService service) {
        this.service = service;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        LOGGER.info("session created for a request received from the client: {}",session.getHandshakeInfo().getUri());

        return session.send(new DataExchangeBuilder<>
                (MissionEventsRequest.class, MissionEvent.class)
                .setPreInput(session.receive().map(WebSocketMessage::getPayloadAsText))
                .setGenerator((request) -> service.getFlux(request.getMissionExecutionId()))
                .setGeneratorExceptionHandler(MissionException::new)
                .build().map(session::textMessage));
    }
}
