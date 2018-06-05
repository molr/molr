package cern.molr.server.api;

import cern.molr.commons.request.MissionCommandRequest;
import cern.molr.commons.request.client.ServerInstantiationRequest;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionEvent;
import cern.molr.commons.response.SupervisorState;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * It represents a remote supervisor used by MolR server to communicate with a single supervisor
 *
 * @author yassine-kr
 */
public interface RemoteMoleSupervisor {

    /**
     * A method which should send an instantiation request to the supervisor
     *
     * @param serverRequest the request sent by the client to mMolR server
     * @param missionExecutionId the mission ID of the execution
     * @param <I>                the argument type
     *
     * @return a publisher of events triggered by the mission execution
     */
    <I> Flux<MissionEvent> instantiate(ServerInstantiationRequest<I> serverRequest, String missionExecutionId);


    /**
     * A method which should send a command request to the impl
     *
     * @param commandRequest
     *
     * @return a publisher of the command response
     */
    Mono<CommandResponse> instruct(MissionCommandRequest commandRequest);

    /**
     * A method which should return the supervisor state
     *
     * @return optional, empty if the method fails to get the state
     */
    Optional<SupervisorState> getSupervisorState();

}
