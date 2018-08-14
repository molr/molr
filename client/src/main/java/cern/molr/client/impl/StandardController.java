package cern.molr.client.impl;

import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MolrClientToServer;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import org.reactivestreams.Publisher;

import java.util.Objects;

/**
 * Standard client controller which can be extended to create custom controllers
 * @author yassine-kr
 */
public class StandardController implements ClientMissionController {

    /**
     * The client used to perform network connections
     */
    private MolrClientToServer client;

    private String missionName;
    private String missionId;

    public StandardController(MolrClientToServer client, String missionName, String missionId) {
        this.client = client;
        this.missionName = missionName;
        this.missionId = missionId;

        Objects.requireNonNull(client);
        Objects.requireNonNull(missionName);
        //The controller should not be created until retrieving the mission execution id
        Objects.requireNonNull(missionId);
    }

    @Override
    public Publisher<MissionEvent> getEventsStream() {
        return client.getEventsStream(missionName, missionId);
    }

    @Override
    public Publisher<MissionState> getStatesStream() {
        return client.getStatesStream(missionName, missionId);
    }

    @Override
    public Publisher<CommandResponse> instruct(MissionCommand command) {
        return client.instruct(missionName, missionId, command);
    }
}
