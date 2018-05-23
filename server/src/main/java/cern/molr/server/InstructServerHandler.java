package cern.molr.server;

import cern.molr.exception.UnknownMissionException;
import cern.molr.commons.response.CommandResponse;
import cern.molr.mole.supervisor.MissionCommandRequest;
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
 * WebSocket Spring Handler which handles websoscket requests for instructing a mission execution
 * @author yassine-kr
 */
@Component
public class InstructServerHandler implements WebSocketHandler {

    private final ServerRestExecutionService service;

    /**
     * A processor which receives flux events and resend them to client
     * TODO choose the best implementation to use
     */
    private FluxProcessor<String,String> processor=TopicProcessor.create();


    public InstructServerHandler(ServerRestExecutionService service) {
        this.service = service;
    }

    /**
     * @param session websocket session
     * @return task which sends flux of
     * TODO instead of returning a plain text when the serialization fails, a json representing the exception should be returned
     */
    @Override
    public Mono<Void> handle(WebSocketSession session) {


        ObjectMapper mapper=new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return session.send(processor.map(session::textMessage))
                .and((session.receive().<Optional<MissionCommandRequest>>map((message)->{
                    try {
                        return Optional.ofNullable(mapper.readValue(message.getPayloadAsText(),MissionCommandRequest.class));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return Optional.empty();
                    }
                })).doOnNext((optionalCommand)->{
                    optionalCommand.ifPresent((command)-> {
                        try {
                            service.instruct(command).map((result)->{
                                try {
                                    return mapper.writeValueAsString(result);
                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                    try {
                                        return mapper.writeValueAsString(new CommandResponse.CommandResponseFailure(e));
                                    } catch (JsonProcessingException e1) {
                                        e1.printStackTrace();
                                        return "unable to serialize a failure result: source: "+e.getMessage();
                                    }
                                }
                            }).subscribe(processor::onNext);
                        } catch (UnknownMissionException e) {
                            e.printStackTrace();
                            try {
                                processor.onNext(mapper.writeValueAsString(new CommandResponse.CommandResponseFailure(e)));
                            } catch (JsonProcessingException e1) {
                                e1.printStackTrace();
                                processor.onNext("unable to serialize a failure result: source: unknown mission exception");
                            }
                        }
                    });
                    if(!optionalCommand.isPresent()){
                        try {
                            processor.onNext(mapper.writeValueAsString(new CommandResponse.CommandResponseFailure(new Exception("Unable to deserialize request"))));
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            processor.onNext("unable to serialize a failure result: source: unable to deserialize request");
                        }
                    }
                }));
    }
}
