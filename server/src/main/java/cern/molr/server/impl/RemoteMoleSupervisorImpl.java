package cern.molr.server.impl;

import cern.molr.commons.request.MissionCommandRequest;
import cern.molr.commons.request.client.ServerInstantiationRequest;
import cern.molr.commons.request.server.SupervisorStateRequest;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionEvent;
import cern.molr.commons.response.SupervisorState;
import cern.molr.commons.response.SupervisorStateResponse;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClientImpl;
import cern.molr.server.api.RemoteMoleSupervisor;
import org.reactivestreams.Publisher;
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
    private MolrWebSocketClientImpl socketClient;

    public RemoteMoleSupervisorImpl(String host, int port) {
        this.socketClient = new MolrWebSocketClientImpl(host, port);
        this.client = new MolrWebClient(host, port);
    }

    @Override
    public <I> Publisher<MissionEvent> instantiate(ServerInstantiationRequest<I> serverRequest, String
            missionExecutionId) {
        return socketClient.instantiate(serverRequest.getMissionName(), missionExecutionId, serverRequest.getArgs());
    }

    @Override
    public Publisher<CommandResponse> instruct(MissionCommandRequest commandRequest) {
        return socketClient.instruct("unknown",commandRequest.getMissionId(),commandRequest.getCommand());
    }


    //TODO when MolR server is unable to get the state of the supervisor, it should unregister it from supervisors manager
    @Override
    public Optional<SupervisorState> getSupervisorState() {
            return client.getState().block().match((throwable) -> {
                throwable.printStackTrace();
                return Optional.empty();
            }, Optional::<SupervisorState>ofNullable);

    }

}
