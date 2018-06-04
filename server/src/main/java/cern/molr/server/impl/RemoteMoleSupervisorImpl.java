package cern.molr.server.impl;

import cern.molr.commons.events.MissionException;
import cern.molr.commons.request.MissionCommandRequest;
import cern.molr.commons.request.server.SupervisorInstantiationRequest;
import cern.molr.commons.request.server.SupervisorStateRequest;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionEvent;
import cern.molr.commons.response.SupervisorState;
import cern.molr.commons.response.SupervisorStateResponse;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.server.api.RemoteMoleSupervisor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Function;

/**
 * Implementation of a Remote Supervisor which is able to return its state using network
 *
 * @author yassine-kr
 */
public class RemoteMoleSupervisorImpl implements RemoteMoleSupervisor {

    protected MolrWebClient client;
    private MolrWebSocketClient socketClient;

    public RemoteMoleSupervisorImpl(String host, int port) {
        this.socketClient = new MolrWebSocketClient(host, port);
        this.client = new MolrWebClient(host, port);
    }

    @Override
    public <I> Flux<MissionEvent> instantiate(String missionClassName, I args, String missionExecutionId) {
        SupervisorInstantiationRequest<I> request =
                new SupervisorInstantiationRequest<I>(missionExecutionId,
                        missionClassName, args);
        return socketClient.receiveFlux("/instantiate", MissionEvent.class, request)
                .map((tryElement) -> tryElement.match(MissionException::new, Function.identity()));
    }

    @Override
    public Mono<CommandResponse> instruct(MissionCommandRequest commandRequest) {
        return socketClient.receiveMono("/instruct", CommandResponse.class, commandRequest)
                .doOnError(Throwable::printStackTrace).map((tryElement) -> tryElement
                        .match(CommandResponse.CommandResponseFailure::new, Function.identity()));
    }


    //TODO when MolR server is unable to get the state of the supervisor, it should unregister it from supervisors manager
    @Override
    public Optional<SupervisorState> getSupervisorState() {
        try {
            return client.post("/getState", SupervisorStateRequest.class, new SupervisorStateRequest(),
                    SupervisorStateResponse.class).get().match((throwable) -> {
                throwable.printStackTrace();
                return Optional.empty();
            }, Optional::<SupervisorState>ofNullable);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
