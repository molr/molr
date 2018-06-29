package cern.molr.server.api;

import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.SupervisorState;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A client service which allows to perform some defined requests to a a supervisor
 *
 * @author yassine-kr
 */
public interface MolrServerToSupervisor {

    /**
     * It is an synchronous method which sends the state request to the supervisor and waits for the response
     * It should return empty if there was a connection error or the response was a failure
     *
     * @return optional state supervisor
     */
    Optional<SupervisorState> getState();

    /**
     * Should send an instantiation request to the server and return an events stream form the server
     */
    <I> Publisher<MissionEvent> instantiate(String missionName, String missionId, I missionArguments);

    /**
     * Should send a command request to the server and return a stream of one element containing the command response
     */
    Publisher<CommandResponse> instruct(String missionName, String missionId, MissionCommand command);

}
