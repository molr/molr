/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.impl.web;

import cern.molr.commons.api.request.supervisor.SupervisorRegisterRequest;
import cern.molr.commons.api.request.supervisor.SupervisorUnregisterRequest;
import cern.molr.commons.api.response.SupervisorRegisterResponse;
import cern.molr.commons.api.response.SupervisorUnregisterResponse;
import cern.molr.commons.web.MolrConfig;
import cern.molr.commons.web.WebFluxRestClient;
import cern.molr.supervisor.api.web.MolrSupervisorToServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * Implementation using Spring WebFlux
 * @author yassine-kr
 */
public class MolrSupervisorToServerImpl implements MolrSupervisorToServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(MolrSupervisorToServerImpl.class);

    private WebFluxRestClient restClient;

    public MolrSupervisorToServerImpl(String host, int port) {
        restClient = new WebFluxRestClient(host, port);
    }

    @Override
    public String register(String host, int port, List<String> acceptedMissions) {
        SupervisorRegisterRequest request = new SupervisorRegisterRequest(host, port, acceptedMissions);
        return restClient.post(MolrConfig.REGISTER_PATH, SupervisorRegisterRequest.class, request,
                SupervisorRegisterResponse
                .class).map((tryResponse) -> tryResponse.match(throwable -> {
                    throw new CompletionException(throwable);
                }
                , Function.identity())).block().getSupervisorId();
    }

    @Override
    public void unregister(String supervisorId) {
        SupervisorUnregisterRequest request = new SupervisorUnregisterRequest(supervisorId);
        restClient.post(MolrConfig.UNREGISTER_PATH, SupervisorUnregisterRequest.class, request,
                SupervisorUnregisterResponse.class)
                .block();
    }
}