/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.impl;

import cern.molr.client.api.ClientMissionController;
import cern.molr.client.api.MissionExecutionService;
import cern.molr.commons.events.MissionException;
import cern.molr.commons.request.MissionCommand;
import cern.molr.commons.request.MissionCommandRequest;
import cern.molr.commons.request.client.MissionEventsRequest;
import cern.molr.commons.request.client.ServerInstantiationRequest;
import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.InstantiationResponse;
import cern.molr.commons.response.InstantiationResponseBean;
import cern.molr.commons.response.MissionEvent;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Implementation used by the operator to interact with the server
 * The constructor searches for MolR address (host and port) in "config.properties"
 * The default values are "http://localhost" and "8000"
 *
 * @author yassine-kr
 */
public class MissionExecutionServiceImpl implements MissionExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissionExecutionServiceImpl.class);

    private MolrWebClient client;
    private MolrWebSocketClient clientSocket;

    public MissionExecutionServiceImpl() {
        try (InputStream input = MissionExecutionServiceImpl.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            Properties properties = new Properties();

            properties.load(input);

            String host = properties.getProperty("host");
            Objects.requireNonNull(host);

            int port = Integer.parseInt(properties.getProperty("port"));

            client = new MolrWebClient(host, port);
            clientSocket = new MolrWebSocketClient(host, port);

        } catch (Exception error) {
            LOGGER.error("error while trying to get client properties", error);
            client = new MolrWebClient("http://localhost", 8000);
            clientSocket = new MolrWebSocketClient("http://localhost", 8000);
        }
    }

    public MissionExecutionServiceImpl(String host, int port) {
        client = new MolrWebClient(host, port);
        clientSocket = new MolrWebSocketClient(host, port);
    }

    @Override
    public <I> Publisher<ClientMissionController> instantiate(String missionName, I args) {

        ServerInstantiationRequest<I> execRequest = new ServerInstantiationRequest<>(missionName, args);
        return client.post("/instantiate", ServerInstantiationRequest.class, execRequest,
                        InstantiationResponse.class)
                        .map(tryResp -> tryResp.match((Throwable e) -> { throw new CompletionException(e); },
                                InstantiationResponseBean::getMissionExecutionId))
                        .<ClientMissionController>map(missionExecutionId -> new ClientMissionController() {
                            @Override
                            public Publisher<MissionEvent> getEventsStream() {
                                MissionEventsRequest eventsRequest = new MissionEventsRequest(missionExecutionId);
                                return clientSocket.receiveFlux("/getEventsStream", MissionEvent.class, eventsRequest)
                                        .doOnError((e) ->
                                                LOGGER.error("error in events stream [mission execution Id: {}, " +
                                                                "mission name: {}]", missionExecutionId,
                                                        execRequest.getMissionName(), e))
                                        .map((tryElement) -> tryElement.match(MissionException::new, Function.identity()));
                            }

                            @Override
                            public Mono<CommandResponse> instruct(MissionCommand command) {
                                MissionCommandRequest commandRequest = new MissionCommandRequest(missionExecutionId,
                                        command);
                                return clientSocket.
                                        receiveMono("/instruct", CommandResponse.class, commandRequest)
                                        .doOnError((e) ->
                                                LOGGER.error("error in command stream [mission execution Id: {}, " +
                                                                "mission name: {}, command: {}]",
                                                        missionExecutionId,
                                                        execRequest.getMissionName(), command, e))
                                        .map((tryElement) ->
                                                tryElement.match(CommandResponse.CommandResponseFailure::new,
                                                        Function.identity()));
                            }
                        }).doOnError((e) ->
                        LOGGER.error("error while sending an instantiation request [mission name: {}]",
                        execRequest.getMissionName(), e.getCause()));
    }
}
