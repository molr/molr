/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.impl;

import cern.molr.client.api.MolrClientToServer;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.request.MissionCommandRequest;
import cern.molr.commons.api.request.client.ServerInstantiationRequest;
import cern.molr.commons.api.response.*;
import cern.molr.commons.web.MolrConfig;
import cern.molr.commons.web.WebFluxRestClient;
import cern.molr.commons.web.WebFluxWebSocketClient;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * Implementation using Spring WebFlux
 *
 * @author yassine-kr
 */
public class MolrClientToServerImpl implements MolrClientToServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MolrClientToServerImpl.class);

    private WebFluxRestClient restClient;
    private WebFluxWebSocketClient socketClient;

    public MolrClientToServerImpl(String host, int port) {
        restClient = new WebFluxRestClient(host, port);
        socketClient = new WebFluxWebSocketClient(host, port);
    }

    @Override
    public <I, C> Publisher<C> instantiate(String missionName, I missionArguments, Function<String, C> mapper) {
        ServerInstantiationRequest<I> request = new ServerInstantiationRequest<>(missionName, missionArguments);
        return restClient.post(MolrConfig.INSTANTIATE_PATH, ServerInstantiationRequest.class, request,
                InstantiationResponse.class)
                .map(tryResponse -> tryResponse.match((Throwable e) -> {
                            throw new CompletionException(e);
                        },
                        InstantiationResponseBean::getMissionId))
                .map(mapper)
                .doOnError((e) -> LOGGER.error("error while sending an instantiation request [mission name: {}]",
                        missionName, e.getCause()));
    }

    @Override
    public Publisher<MissionEvent> getEventsStream(String missionName, String missionId) {
        return socketClient.receiveFlux(MolrConfig.EVENTS_STREAM_PATH, MissionEvent.class, missionId)
                .doOnError((e) ->
                        LOGGER.error("error in events stream [mission execution Id: {}, mission name: {}]", missionId,
                                missionName, e));
    }

    @Override
    public Publisher<MissionState> getStatesStream(String missionName, String missionId) {
        return socketClient.receiveFlux(MolrConfig.STATES_STREAM_PATH, MissionState.class, missionId)
                .doOnError((e) ->
                        LOGGER.error("error in states stream [mission execution Id: {}, mission name: {}]", missionId,
                                missionName, e));
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