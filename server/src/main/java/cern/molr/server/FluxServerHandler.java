package cern.molr.server;

import cern.molr.commons.events.MissionException;
import cern.molr.commons.request.client.MissionEventsRequest;
import cern.molr.commons.response.ManuallySerializable;
import cern.molr.commons.response.MissionEvent;
import cern.molr.commons.web.DataExchangeBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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

    private final ServerRestExecutionService service;

    public FluxServerHandler(ServerRestExecutionService service) {
        this.service = service;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        return session.send(new DataExchangeBuilder<>
                (MissionEventsRequest.class, MissionEvent.class)
                .setPreInput(session.receive().map(WebSocketMessage::getPayloadAsText))
                .setGenerator((request) ->
                        service.getFlux(request.getMissionExecutionId()))
                .setGeneratorExceptionHandler(MissionException::new
                        , throwable -> ManuallySerializable.serializeArray(
                                new MissionException("unable to serialize a mission exception, source: unknown mission"))
                ).setGeneratingExceptionHandler(MissionException::new
                        , throwable -> ManuallySerializable.serializeArray(
                                new MissionException("unable to serialize a mission exception, " +
                                        "source: unable to serialize an event"))
                ).setReceivingExceptionHandler(MissionException::new
                        , throwable -> ManuallySerializable.serializeArray(
                                new MissionException("unable to serialize a mission exception, " +
                                        "source: unable to deserialize the request")))
                .build().map(session::textMessage));
    }
}
