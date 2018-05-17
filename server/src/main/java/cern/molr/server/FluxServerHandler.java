package cern.molr.server;

import cern.molr.exception.UnknownMissionException;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.server.request.MissionEventsRequest;
import cern.molr.type.ManuallySerializable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.TopicProcessor;

import java.io.IOException;
import java.util.Optional;

/**
 * WebSocket Spring Handler which handles websoscket requests for getting flux of events concerning a mission execution
 * @author yassine
 */
@Component
public class FluxServerHandler implements WebSocketHandler {

    private final ServerRestExecutionService service;

    public FluxServerHandler(ServerRestExecutionService service) {
        this.service = service;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {


        ObjectMapper mapper=new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        /**
         * A processor which receives flux events and resend them to client
         * TODO choose the best implementation to use
         */
        FluxProcessor<String,String> processor=TopicProcessor.create();

        return session.send(processor.map(session::textMessage))
                .and((session.receive().take(1).<Optional<MissionEventsRequest>>map((message)->{
            try {
                return Optional.ofNullable(mapper.readValue(message.getPayloadAsText(),MissionEventsRequest.class));
            } catch (IOException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        })).doOnNext((optionalRequest)->{
            optionalRequest.ifPresent((request)-> {
                try {
                    service.getFlux(request.getMissionExecutionId()).map((event)->{
                        try {
                            return mapper.writeValueAsString(event);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            try {
                                return mapper.writeValueAsString(new RunEvents.MissionException(e));
                            } catch (JsonProcessingException e1) {
                                e1.printStackTrace();
                                return ManuallySerializable.serializeArray(new RunEvents.MissionException("unable to serialize a mission exception, source: unable to serialize an event"));
                            }
                        }
                    }).doOnComplete(processor::onComplete).subscribe(processor::onNext);
                }catch(UnknownMissionException e){
                    try {
                        processor.onNext(mapper.writeValueAsString(new RunEvents.MissionException(e)));
                        processor.onComplete();
                    } catch (JsonProcessingException e1) {
                        e1.printStackTrace();
                        processor.onNext(ManuallySerializable.serializeArray(new RunEvents.MissionException("unable to serialize a mission exception, source: unknown mission")));
                        processor.onComplete();
                    }
                }
            });
            if(!optionalRequest.isPresent()){
                try {
                    processor.onNext(mapper.writeValueAsString(new RunEvents.MissionException(new Exception("unable to deserialize request"))));
                    processor.onComplete();
                } catch (JsonProcessingException e1) {
                    e1.printStackTrace();
                    processor.onNext(ManuallySerializable.serializeArray(new RunEvents.MissionException("unable to serialize a mission exception, source: unable to deserialize the request")));
                    processor.onComplete();
                }
            }
        }));
    }
}
