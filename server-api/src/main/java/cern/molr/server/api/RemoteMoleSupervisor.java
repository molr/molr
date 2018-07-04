package cern.molr.server.api;

import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.request.client.ServerInstantiationRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.SupervisorState;
import org.reactivestreams.Publisher;

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
     * @param serverRequest the request sent by the client to MolR server
     * @param missionId     the ID of the execution
     * @param <I>           the argument type
     *
     * @return a publisher of events triggered by the mission execution
     */
    <I> Publisher<MissionEvent> instantiate(ServerInstantiationRequest<I> serverRequest, String missionId);


    /**
     * A method which should send a command request to the impl
     *
     * @param commandRequest The command request
     *
     * @return a publisher of the command response, it contains one element
     */
    Publisher<CommandResponse> instruct(MissionCommandRequest commandRequest);

    /**
     * A method which should return the supervisor state
     *
     * @return optional, empty if the method fails to get the state
     */
    Optional<SupervisorState> getSupervisorState();

    /**
     * Add a {@link TimeOutStateListener}
     * @param listener the listener to add
     */
    void addStateAvailabilityListener(TimeOutStateListener listener);

}
