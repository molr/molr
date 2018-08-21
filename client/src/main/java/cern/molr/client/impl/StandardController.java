package cern.molr.client.impl;

import cern.molr.client.api.ClientControllerData;
import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MolrClientToServer;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import cern.molr.commons.commands.MissionControlCommand;
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

    public StandardController(ClientControllerData clientControllerData) {
        this.client = clientControllerData.getClient();
        this.missionName = clientControllerData.getMissionName();
        this.missionId = clientControllerData.getMissionId();

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

    public Publisher<CommandResponse> start() {
        return instruct(new MissionControlCommand(MissionControlCommand.Command.START));
    }

    public Publisher<CommandResponse> terminate() {
        return instruct(new MissionControlCommand(MissionControlCommand.Command.TERMINATE));
    }

}
