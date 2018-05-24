package cern.molr.supervisor.impl;

import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mission.Mission;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.*;
import cern.molr.supervisor.request.SupervisorMissionExecutionRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Proxy implementation of {@link MoleSupervisor} which communicates with a remote supervisor
 * @author yassine-kr
 */
public class MoleSupervisorProxy implements MoleSupervisor {

    private MolrWebSocketClient socketClient;
    protected MolrWebClient client;

    public MoleSupervisorProxy(String host, int port) {
        this.socketClient = new MolrWebSocketClient(host, port);
        this.client =new MolrWebClient(host,port);
    }


    @Override
    public <I> Flux<MoleExecutionEvent> instantiate(Mission mission, I args, String missionExecutionId) {
        SupervisorMissionExecutionRequest<I> request=new SupervisorMissionExecutionRequest<I>(missionExecutionId,mission.getMoleClassName(),mission.getMissionDefnClassName(),args);
        return socketClient.receiveFlux("/instantiate",MoleExecutionEvent.class,request).map((tryElement)->tryElement.match(RunEvents.MissionException::new,Function.identity()));
    }

    @Override
    public Mono<MoleExecutionCommandResponse> instruct(MoleExecutionCommand command) {
        return socketClient.receiveMono("/instruct",MoleExecutionCommandResponse.class,command).doOnError(Throwable::printStackTrace).map((tryElement)->tryElement.match(CommandResponse.CommandResponseFailure::new, Function.identity()));
    }
}
