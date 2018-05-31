package cern.molr.supervisor.impl;

import cern.molr.commons.SupervisorState;
import cern.molr.commons.response.CommandResponse;
import cern.molr.exception.UnknownMissionException;
import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.spawner.run.RunSpawner;
import cern.molr.mole.supervisor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * An Implementation of {@link MoleSupervisor} which manages mission executions which does not manage the state
 * @author yassine-kr
 */
public class MoleSupervisorImpl implements MoleSupervisor {

    protected SupervisorSessionsManager sessionsManager=new SupervisorSessionsManagerImpl();
    private static final Logger LOGGER = LoggerFactory.getLogger(MoleSupervisorImpl.class);

    /* TODO there is a moment between instantiating and adding the listener to the controller, the JVM instantiated
     * TODO event could be missed, is this behaviour acceptable?

     */
    @Override
    public <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId){
        try {
            MissionSession session;
            RunSpawner<I> spawner=new RunSpawner<>();
            session=spawner.spawnMoleRunner(mission,args);
            sessionsManager.addSession(missionExecutionId,session);
            session.getController().addMoleExecutionListener((event)->{
                if(event instanceof RunEvents.JVMDestroyed) {
                    sessionsManager.removeSession(session);
                    try {
                        session.getController().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return Flux.create((FluxSink<MoleExecutionEvent> emitter)->{
                session.getController().addMoleExecutionListener((event)->{
                    emitter.next(event);
                    LOGGER.info("Event Notification from JVM controller: {}", event);
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
    public Mono<MoleExecutionCommandResponse> instruct(MissionCommandRequest commandRequest) {
        return Mono.just(sessionsManager.getSession(commandRequest.getMissionId()).map((session)->{
            MoleExecutionCommandResponse response=session.getController().sendCommand(commandRequest.getCommand());
            LOGGER.info("Receiving command response from JVM controller: {}", response);
            return response;
        }).orElse(new CommandResponse.CommandResponseFailure(new UnknownMissionException("No such mission running"))));
    }

    @Override
    public SupervisorState getSupervisorState() {
        return null;
    }
}
