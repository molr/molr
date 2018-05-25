/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.serviceimpl;

import cern.molr.commons.response.CommandResponse;
import cern.molr.commons.response.MissionExecutionResponse;
import cern.molr.commons.response.MissionExecutionResponseBean;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mission.controller.ClientMissionController;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MissionCommandRequest;
import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.server.request.MissionEventsRequest;
import cern.molr.server.request.ServerMissionExecutionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * Implementation used by the operator to interact with the server
 * The constructor searches for MolR address (host and port) in "config.properties"
 * The default values are "localhost" and "8000"
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
            int port = Integer.parseInt(properties.getProperty("port"));

            client = new MolrWebClient(host, port);
            clientSocket = new MolrWebSocketClient(host, port);

        } catch (Exception e) {
            LOGGER.error("error while trying to get client properties", e);
            client = new MolrWebClient("localhost", 8000);
            clientSocket = new MolrWebSocketClient("localhost", 8000);
        }
    }

    public MissionExecutionServiceImpl(String host, int port) {
        client = new MolrWebClient(host, port);
        clientSocket = new MolrWebSocketClient(host, port);
    }

    @Override
    public <I> Mono<ClientMissionController> instantiate(String missionDefnClassName, I args) {
        ServerMissionExecutionRequest<I> execRequest = new ServerMissionExecutionRequest<>(missionDefnClassName, args);
        return Mono.<ClientMissionController>create((emitter) -> {
            try {
                emitter.success(client.post("/instantiate", ServerMissionExecutionRequest.class, execRequest,
                        MissionExecutionResponse.class)
                        .thenApply(tryResp -> tryResp.match(
                                (Throwable e) -> {
                                    throw new CompletionException(e);
                                }, MissionExecutionResponseBean::getMissionExecutionId))
                        .thenApply(missionExecutionId -> new ClientMissionController() {
                            @Override
                            public Flux<MoleExecutionEvent> getFlux() {
                                MissionEventsRequest eventsRequest = new MissionEventsRequest(missionExecutionId);
                                return clientSocket.receiveFlux("/getFlux", MoleExecutionEvent.class, eventsRequest)
                                        .doOnError((e) -> LOGGER.error("error while receiving events flux", e)).map(
                                                (tryElement)
                                                        -> tryElement
                                                        .match(RunEvents.MissionException::new, Function.identity()));
                            }

                            @Override
                            public Mono<MoleExecutionCommandResponse> instruct(MoleExecutionCommand command) {
                                MissionCommandRequest commandRequest = new MissionCommandRequest(missionExecutionId,
                                        command);
                                return clientSocket.
                                        receiveMono("/instruct", MoleExecutionCommandResponse.class, commandRequest)
                                        .doOnError((e) -> LOGGER.error("error while receiving a command response", e))
                                        .map((tryElement) ->
                                                tryElement.match(CommandResponse.CommandResponseFailure::new,
                                                        Function.identity()));
                            }
                        }).get());
            } catch (InterruptedException | ExecutionException e) {
                emitter.error(e.getCause());
            }
        }).subscribeOn(Schedulers.fromExecutorService(Executors.newSingleThreadExecutor()));
    }
}
