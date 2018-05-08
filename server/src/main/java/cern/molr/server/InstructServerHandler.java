package cern.molr.server;

import cern.molr.exception.UnknownMissionException;
import cern.molr.mole.spawner.debug.ResponseCommand;
import cern.molr.mole.supervisor.MoleExecutionCommand;
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
import java.time.Duration;
import java.util.Optional;

/**
 * WebSocket Spring Handler which handles websoscket requests for instructing a mission execution
 * @author yassine
 */
@Component
public class InstructServerHandler implements WebSocketHandler {

    private final ServerRestExecutionServiceNew service;

    public InstructServerHandler(ServerRestExecutionServiceNew service) {
        this.service = service;
    }

    /**
     * @param session websocket session
     * @return task which sends flux of
     * TODO return more meaningful exceptions, create class type for each type of exception evemt instead of using MissionException event
     */
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
                .and((session.receive().take(1).<Optional<MoleExecutionCommand>>map((message)->{
                    try {
                        return Optional.ofNullable(mapper.readValue(message.getPayloadAsText(),MoleExecutionCommand.class));
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
                                        return mapper.writeValueAsString(new ResponseCommand.ResponseCommandFailure(e));
                                    } catch (JsonProcessingException e1) {
                                        e1.printStackTrace();
                                        return "unable to serialize a failure result: source: "+e.getMessage();
                                    }
                                }
                            }).subscribe((t)->{
                                processor.onNext(t);
                                processor.onComplete();
                            });
                        } catch (UnknownMissionException e) {
                            e.printStackTrace();
                            try {
                                processor.onNext(mapper.writeValueAsString(new ResponseCommand.ResponseCommandFailure(e)));
                                processor.onComplete();
                            } catch (JsonProcessingException e1) {
                                e1.printStackTrace();
                                processor.onNext("unable to serialize a failure result: source: unknown mission exception");
                                processor.onComplete();
                            }
                        }
                    });
                    if(!optionalCommand.isPresent()){
                        try {
                            processor.onNext(mapper.writeValueAsString(new ResponseCommand.ResponseCommandFailure(new Exception("Unable to deserialize request"))));
                            processor.onComplete();
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                            processor.onNext("unable to serialize a failure result: source: unable to serialize sent command");
                            processor.onComplete();
                        }
                    }
                }));
    }
}
