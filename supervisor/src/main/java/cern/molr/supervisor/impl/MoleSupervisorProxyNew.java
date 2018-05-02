package cern.molr.supervisor.impl;

import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.supervisor.*;
import cern.molr.sample.mission.Fibonacci;
import cern.molr.sample.mole.IntegerFunctionMole;
import cern.molr.supervisor.request.MissionExecutionRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        return client.receiveFlux("/instantiate",MoleExecutionEvent.class,request);
    }

    @Override
    public Mono<MoleExecutionResponseCommand> instruct(MoleExecutionCommand command) {
        return client.receiveMono("/instruct",MoleExecutionResponseCommand.class,command);
    }
}
