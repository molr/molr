package cern.molr.supervisor.impl;

import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.spawner.run.RunSpawner;
import cern.molr.mole.supervisor.*;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

/**
 * Implementation of new interface {@link MoleSupervisorNew}
 * TODO remove "New" from class name
 * This implementation manage only one mission execution; mission Id is not used ofr the moment
 * @author yassine
 */
public class MoleSupervisorImplNew implements MoleSupervisorNew {

    private MoleSession session;

    @Override
    public <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId) {
        try {
            RunSpawner<I> spawner=new RunSpawner<>();
            session=spawner.spawnMoleRunner(mission,args);
            return Flux.create((FluxSink<MoleExecutionEvent> emitter)->{
                session.getController().addMoleExecutionListener(emitter::next);
            });
        } catch (Exception e) {
            e.printStackTrace();
            return Flux.just(new RunEvents.MissionException(e));
        }
    }

    @Override
    public Mono<MoleExecutionRequestCommandResult> instruct(MoleExecutionCommand command, String missionExecutionId) {
        return Mono.just(session.getController().sendCommand(command));
    }
}
