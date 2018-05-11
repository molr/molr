package cern.molr.supervisor.impl;

import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mission.Mission;
import cern.molr.mole.supervisor.*;
import cern.molr.supervisor.request.SupervisorMissionExecutionRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Proxy implementation of {@link MoleSupervisor} which communicates with a remote supervisor
 * @author yassine
 */
public class MoleSupervisorProxy implements MoleSupervisor {

    private MolrWebSocketClient client;

    public MoleSupervisorProxy(String host, int port) {
        this.client = new MolrWebSocketClient(host, port);
    }


    @Override
    public <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId) {
        SupervisorMissionExecutionRequest<I> request=new SupervisorMissionExecutionRequest<I>(missionExecutionId,mission.getMoleClassName(),mission.getMissionDefnClassName(),args);
        return client.receiveFlux("/instantiate",MoleExecutionEvent.class,request);
    }

    @Override
    public Mono<MoleExecutionCommandResponse> instruct(MoleExecutionCommand command) {
        return client.receiveMono("/instruct",MoleExecutionCommandResponse.class,command);
    }
}
