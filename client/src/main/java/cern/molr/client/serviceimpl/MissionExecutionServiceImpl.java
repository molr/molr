/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.serviceimpl;

import cern.molr.commons.response.*;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mission.controller.ClientMissionController;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.server.request.MissionEventsRequest;
import cern.molr.server.request.ServerMissionExecutionRequest;
import cern.molr.mole.supervisor.MissionCommandRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletionException;

/**
 * Implementation used by the operator to interact with the server
 * The constructor searches for MolR address (host and port) in "config.properties"
 * The default values are "localhost" and "8000"
 * 
 * @author yassine-kr-kr
 */
public class MissionExecutionServiceImpl implements MissionExecutionService {

    private MolrWebClient client;
    private MolrWebSocketClient clientSocket;

    public MissionExecutionServiceImpl() {
        try (InputStream input=MissionExecutionServiceImpl.class.getClassLoader().getResourceAsStream("config.properties")){

            Properties properties = new Properties();

            properties.load(input);

            String host= properties.getProperty("host");
            int port= Integer.parseInt(properties.getProperty("port"));

            client = new MolrWebClient(host,port);
            clientSocket=new MolrWebSocketClient(host,port);

        } catch (Exception e) {
            e.printStackTrace();
            client = new MolrWebClient("localhost",8000);
            clientSocket=new MolrWebSocketClient("localhost",8000);
        }
    }

    public MissionExecutionServiceImpl(String host,int port){
        client = new MolrWebClient(host,port);
        clientSocket=new MolrWebSocketClient(host,port);
    }

    @Override
    public <I> Mono<ClientMissionController> instantiate(String missionDefnClassName, I args) {
        ServerMissionExecutionRequest<I> execRequest = new ServerMissionExecutionRequest<>(missionDefnClassName, args);
        return Mono.fromFuture(client.post("/instantiate", ServerMissionExecutionRequest.class, execRequest, MissionExecutionResponse.class)
                .thenApply(tryResp -> tryResp.match(
                        (Throwable e) -> {
                            throw new CompletionException(e);
                            }, MissionExecutionResponseBean::getMissionExecutionId))
                .thenApply(missionExecutionId -> new ClientMissionController() {
                            @Override
                            public Flux<MoleExecutionEvent> getFlux() {
                                MissionEventsRequest eventsRequest=new MissionEventsRequest(missionExecutionId);
                                return clientSocket.receiveFlux("/getFlux",MoleExecutionEvent.class,eventsRequest).doOnError(Throwable::printStackTrace);
                            }
                            @Override
                            public Mono<MoleExecutionCommandResponse> instruct(MoleExecutionCommand command) {
                                MissionCommandRequest request=new MissionCommandRequest(missionExecutionId,command);
                                return clientSocket.receiveMono("/instruct",MoleExecutionCommandResponse.class,request);
                            }
                        }));
    }
}
