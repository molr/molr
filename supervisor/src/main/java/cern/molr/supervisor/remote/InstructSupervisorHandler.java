package cern.molr.supervisor.remote;

import cern.molr.commons.response.CommandResponse;
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
import java.util.Optional;

/**
 * WebSocket Spring Handler which handles websoscket requests for instructing JVM. It returns a mono
 * @author yassine
 */
@Component
public class InstructSupervisorHandler implements WebSocketHandler {

    private final RemoteMoleSupervisorService supervisor;

    public InstructSupervisorHandler(RemoteSupervisorService supervisor) {
        this.supervisor = supervisor;
    }

    /**
     * For each command received, it is delegated to supervisor
     * @param session websocket session
     * @return task which sends flux containing the response and send command to supervisor
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
                supervisor.instruct(command).map((result)->{
                    try {
                        return mapper.writeValueAsString(result);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        try {
                            return mapper.writeValueAsString(new CommandResponse.CommandResponseResutFailure(e));
                        } catch (JsonProcessingException e1) {
                            e1.printStackTrace();
                            return "unable to serialize a failure result: source: "+e.getMessage();
                        }
                    }
                }).subscribe((s)->{
                    processor.onNext(s);
                    processor.onComplete();
                });
            });
            if(!optionalCommand.isPresent()){
                try {
                    processor.onNext(mapper.writeValueAsString(new CommandResponse.CommandResponseResutFailure(new Exception("Unable to deserialize request"))));
                    processor.onComplete();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    processor.onNext("unable to serialize a failure result: source: unable to deserialize sent command");
                    processor.onComplete();
                }
            }
        }));
    }
}
