package cern.molr.supervisor.remote;

import cern.molr.commons.response.CommandResponse;
import cern.molr.mole.supervisor.MissionCommandRequest;
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
 * WebSocket Spring Handler which handles websoscket requests for instructing JVM. It returns a mono
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

        ObjectMapper mapper=new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        /**
         * A processor which receives flux events and resend them to client
         * TODO choose the best implementation to use
         */
        FluxProcessor<String,String> processor=TopicProcessor.create();

        return session.send(processor.map(session::textMessage))
                .and((session.receive().take(1).<Optional<MissionCommandRequest>>map((message)->{
            try {
                return Optional.ofNullable(mapper.readValue(message.getPayloadAsText(),MissionCommandRequest.class));
            } catch (IOException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        })).doOnNext((optionalCommand)->{
            optionalCommand.ifPresent((command)-> {
                supervisor.instruct(command).map((result)->{
                    try {
                        return mapper.writeValueAsString(result);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        try {
                            return mapper.writeValueAsString(new CommandResponse.CommandResponseFailure(e));
                        } catch (JsonProcessingException e1) {
                            e1.printStackTrace();
                            return ManuallySerializable.serializeArray(
                                    new CommandResponse.CommandResponseFailure(
                                            "unable to serialize a failure response, " +
                                                    "source: unable to serialize the response"));
                        }
                    }
                }).subscribe((s)->{
                    processor.onNext(s);
                    processor.onComplete();
                });
            });
            if(!optionalCommand.isPresent()){
                try {
                    processor.onNext(mapper.writeValueAsString(
                            new CommandResponse.CommandResponseFailure(
                                    new Exception("Unable to deserialize send command"))));
                    processor.onComplete();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    processor.onNext(ManuallySerializable.serializeArray(
                            new CommandResponse.CommandResponseFailure("unable to serialize a failure response, " +
                                    "source: unable to deserialize sent command")));
                    processor.onComplete();
                }
            }
        }));
    }
}
