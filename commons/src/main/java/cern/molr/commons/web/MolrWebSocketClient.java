package cern.molr.commons.web;

import cern.molr.commons.request.MissionCommand;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionEvent;
import org.reactivestreams.Publisher;

/**
 * A client service which allows to perform certain defined websocket connection to a server
 *
 * @author yassine-kr
 */
public interface MolrWebSocketClient {

    /**
     * Should send an instantiation request to the server and return an events stream form the server
     */
    <I> Publisher<MissionEvent> instantiate(String missionName, String missionId, I args);

    /**
     * Should return an events stream from the server
     */
    Publisher<MissionEvent> getEventsStream(String missionName, String missionId);

    /**
     * Should send a command request to the server and return a stream of one element containing the command response
     */
    Publisher<CommandResponse> instruct(String missionName, String missionId, MissionCommand command);
}
