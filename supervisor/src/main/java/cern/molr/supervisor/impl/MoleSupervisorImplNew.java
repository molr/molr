package cern.molr.supervisor.impl;

import cern.molr.exception.MissionExecutionNotAccepted;
import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.spawner.run.RunSpawner;
import cern.molr.mole.supervisor.*;
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

    protected SupervisorSessionsManager sessionsManager=new SupervisorSessionsManagerImpl();

    @Override
    public <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId){
        try {
            MoleSession session;
            RunSpawner<I> spawner=new RunSpawner<>();
            session=spawner.spawnMoleRunner(mission,args);
            sessionsManager.addSession(missionExecutionId,session);
            session.getController().addMoleExecutionListener((event)->{
                if(event instanceof RunEvents.JVMDestroyed)
                    sessionsManager.removeSession(session);
            });
            return Flux.create((FluxSink<MoleExecutionEvent> emitter)->{
                session.getController().addMoleExecutionListener((event)->{
                    emitter.next(event);
                    if(event instanceof RunEvents.JVMDestroyed)
                        emitter.complete();
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
            return Flux.just(new RunEvents.MissionException(e));
        }
    }

    @Override
    public Mono<MoleExecutionResponseCommand> instruct(MoleExecutionCommand command) {
        MoleSession session=sessionsManager.getSession(command.getMissionId());
        return Mono.just(session.getController().sendCommand(command));
    }
}
