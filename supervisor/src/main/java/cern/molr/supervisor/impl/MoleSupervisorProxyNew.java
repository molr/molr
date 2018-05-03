package cern.molr.supervisor.impl;

import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mission.Mission;
import cern.molr.mole.spawner.debug.ResponseCommand;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.*;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.sample.mole.IntegerFunctionMole;
import cern.molr.supervisor.request.MissionExecutionRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Implementation of new interface {@link MoleSupervisorNew}
 * TODO remove "New" from class name
 * A proxy which communicates with remote supervisor
 * @author yassine
 */
public class MoleSupervisorProxyNew implements MoleSupervisorNew {

    private MolrWebSocketClient client;

    public MoleSupervisorProxyNew(String host, int port) {
        this.client = new MolrWebSocketClient(host, port);
    }


    @Override
    public <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId) {
        MissionExecutionRequest<I> request=new MissionExecutionRequest<I>(missionExecutionId,mission.getMoleClassName(),mission.getMissionDefnClassName(),args);
        return client.receiveFlux("/instantiate",MoleExecutionEvent.class,request).map((tryElement)->tryElement.match(RunEvents.MissionException::new, Function.identity()));
    }

    @Override
    public Mono<MoleExecutionResponseCommand> instruct(MoleExecutionCommand command) {
        return client.receiveMono("/instruct",MoleExecutionResponseCommand.class,command).doOnError(Throwable::printStackTrace).map((tryElement)->tryElement.match(ResponseCommand.ResponseCommandFailure::new, Function.identity()));
    }
}
