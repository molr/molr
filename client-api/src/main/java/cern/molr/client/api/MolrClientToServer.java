package cern.molr.client.api;

import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import org.reactivestreams.Publisher;

import java.util.function.Function;

/**
 * A client service which allows to perform some defined requests to the MolR server
 *
 * @author yassine-kr
 */
public interface MolrClientToServer {

    /**
     * A method which should perform an instantiation request to a server
     *
     * @param missionName      the mission name
     * @param missionArguments the mission arguments
     * @param mapper           the function to apply on the mission id received from the server
     * @param <I>              the argument type
     * @param <C>              the published element type (it is generally a controller)
     *
     * @return a stream of one element
     */
    <I, C> Publisher<C> instantiate(String missionName, I missionArguments, Function<String, C> mapper);

    /**
     * Should return an events stream from the server
     */
    Publisher<MissionEvent> getEventsStream(String missionName, String missionId);

    /**
     * Should return a states stream from the server
     */
    Publisher<MissionState> getStatesStream(String missionName, String missionId);

    /**
     * Should send a command request to the server and return a stream of one element containing the command response
     */
    Publisher<CommandResponse> instruct(String missionName, String missionId, MissionCommand command);

}
