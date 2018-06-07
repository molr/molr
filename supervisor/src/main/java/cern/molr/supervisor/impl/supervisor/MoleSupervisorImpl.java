package cern.molr.supervisor.impl.supervisor;

import cern.molr.commons.events.MissionException;
import cern.molr.commons.events.SessionTerminated;
import cern.molr.commons.exception.UnknownMissionException;
import cern.molr.commons.mission.Mission;
import cern.molr.commons.request.MissionCommandRequest;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionEvent;
import cern.molr.commons.response.SupervisorState;
import cern.molr.supervisor.api.session.MissionSession;
import cern.molr.supervisor.api.supervisor.MoleSupervisor;
import cern.molr.supervisor.api.supervisor.SupervisorSessionsManager;
import cern.molr.supervisor.impl.spawner.JVMSpawner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * An Implementation of {@link MoleSupervisor} which manages mission executions which does not manage the state
 *
 * @author yassine-kr
 */
public class MoleSupervisorImpl implements MoleSupervisor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoleSupervisorImpl.class);
    protected SupervisorSessionsManager sessionsManager = new SupervisorSessionsManagerImpl();

    /* TODO there is a moment between instantiating and adding the listener to the controller, the session instantiated
     * TODO event could be missed, is this behaviour acceptable?

     */
    @Override
    public <I> Flux<MissionEvent> instantiate(Mission mission, I args, String missionExecutionId) {
        try {
            MissionSession session;
            JVMSpawner<I> spawner = new JVMSpawner<>();
            session = spawner.spawnMoleRunner(mission, args);
            sessionsManager.addSession(missionExecutionId, session);
            session.getController().addMoleExecutionListener((event) -> {
                if (event instanceof SessionTerminated) {
                    sessionsManager.removeSession(session);
                    try {
                        session.getController().close();
                    } catch (IOException error) {
                        error.printStackTrace();
                    }
                }
            });
            return Flux.create((FluxSink<MissionEvent> emitter) -> {
                session.getController().addMoleExecutionListener((event) -> {
                    emitter.next(event);
                    LOGGER.info("Event Notification from session controller: {}", event);
                    if (event instanceof SessionTerminated)
                        emitter.complete();
                });
            });
        } catch (Exception error) {
            error.printStackTrace();
            return Flux.just(new MissionException(error));
        }
    }

    @Override
    public Mono<CommandResponse> instruct(MissionCommandRequest commandRequest) {
        return Mono.just(sessionsManager.getSession(commandRequest.getMissionId()).map((session) -> {
            CommandResponse response = session.getController().sendCommand(commandRequest.getCommand());
            LOGGER.info("Receiving command response from the session controller: {}", response);
            return response;
        }).orElse(new CommandResponse.CommandResponseFailure(new UnknownMissionException("No such mission running"))));
    }

    @Override
    public SupervisorState getSupervisorState() {
        return null;
    }
}
