package cern.molr.server.supervisor;

import cern.molr.commons.SupervisorState;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.SupervisorStateResponse;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MissionCommandRequest;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.server.RemoteMoleSupervisor;
import cern.molr.supervisor.request.SupervisorMissionExecutionRequest;
import cern.molr.supervisor.request.SupervisorStateRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

/**
 * Implementation of a Remote Supervisor which is able to return its state using network
 * @author yassine-kr
 */
public class RemoteMoleSupervisorImpl implements RemoteMoleSupervisor {

    private MolrWebSocketClient socketClient;
    protected MolrWebClient client;

    public RemoteMoleSupervisorImpl(String host, int port) {
        this.socketClient = new MolrWebSocketClient(host, port);
        this.client =new MolrWebClient(host,port);
    }

    @Override
    public <I> Flux<MoleExecutionEvent> instantiate(String missionClassName, I args, String missionExecutionId) {
        SupervisorMissionExecutionRequest<I> request=
                new SupervisorMissionExecutionRequest<I>(missionExecutionId,
                        missionClassName,args);
        return socketClient.receiveFlux("/instantiate",MoleExecutionEvent.class,request)
                .map((tryElement)->tryElement.match(RunEvents.MissionException::new,Function.identity()));
    }

    @Override
    public Mono<MoleExecutionCommandResponse> instruct(MissionCommandRequest commandRequest) {
        return socketClient.receiveMono("/instruct",MoleExecutionCommandResponse.class,commandRequest)
                .doOnError(Throwable::printStackTrace).map((tryElement)->tryElement
                        .match(CommandResponse.CommandResponseFailure::new, Function.identity()));
    }


    //TODO when MolR server is unable to get the state of the supervisor, it should unregister it from supervisors manager
    @Override
    public Optional<SupervisorState> getSupervisorState() {
        try {
            return client.post("/getState",SupervisorStateRequest.class,new SupervisorStateRequest(),SupervisorStateResponse.class).get().match((throwable)->{
                throwable.printStackTrace();
                return Optional.empty();
            }, Optional::<SupervisorState>ofNullable);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
