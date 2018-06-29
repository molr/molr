/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server.impl;

import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.request.server.InstantiationRequest;
import cern.molr.commons.api.request.server.SupervisorStateRequest;
import cern.molr.commons.api.response.CommandResponse;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.SupervisorState;
import cern.molr.commons.api.response.SupervisorStateResponse;
import cern.molr.commons.web.MolrConfig;
import cern.molr.commons.web.WebFluxRestClient;
import cern.molr.commons.web.WebFluxWebSocketClient;
import cern.molr.server.api.MolrServerToSupervisor;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Implementation using Spring WebFlux
 *
 * @author yassine-kr
 */
public class MolrServerToSupervisorImpl implements MolrServerToSupervisor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MolrServerToSupervisorImpl.class);

    private WebFluxRestClient restClient;
    private WebFluxWebSocketClient socketClient;

    public MolrServerToSupervisorImpl(String host, int port) {
        restClient = new WebFluxRestClient(host, port);
        socketClient = new WebFluxWebSocketClient(host, port);
    }

    @Override
    public Optional<SupervisorState> getState() {
        try {
            return restClient.post(MolrConfig.GET_STATE_PATH, SupervisorStateRequest.class, new
                            SupervisorStateRequest(),
                    SupervisorStateResponse.class).block().match((throwable) -> {
                LOGGER.error("unable to get the supervisor state", throwable);
                return Optional.empty();
            }, Optional::<SupervisorState>ofNullable);
        } catch (Exception error) {
            LOGGER.error("unable to get the supervisor state", error);
            return Optional.empty();
        }
    }

    @Override
    public <I> Publisher<MissionEvent> instantiate(String missionName, String missionId, I missionArguments) {
        InstantiationRequest<I> request = new InstantiationRequest<>(missionId, missionName, missionArguments);
        return socketClient.receiveFlux(MolrConfig.INSTANTIATE_PATH, MissionEvent.class, request)
                .doOnError((e) ->
                        LOGGER.error("error in instantiation stream [mission execution Id: {}, mission name: {}]",
                                missionId, missionName, e));
    }

    @Override
    public Publisher<CommandResponse> instruct(String missionName, String missionId, MissionCommand command) {
        MissionCommandRequest request = new MissionCommandRequest(missionId, command);
        return socketClient.receiveMono(MolrConfig.INSTRUCT_PATH, CommandResponse.class, request)
                .doOnError((e) ->
                        LOGGER.error("error in command stream [mission execution Id: {}, mission name: {}, command: {}]",
                                missionId, missionName, command, e));
    }

}