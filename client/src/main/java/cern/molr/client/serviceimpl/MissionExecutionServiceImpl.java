/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.serviceimpl;

import cern.molr.commons.response.*;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mission.controller.ClientMissionController;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionCommandResponse;
import cern.molr.server.request.MissionEventsRequest;
import cern.molr.server.request.ServerMissionExecutionRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * Implementation used by the operator to interact with the server
 * 
 * @author yassine
 */
public class MissionExecutionServiceImpl implements MissionExecutionService {

    private MolrWebClient client = new MolrWebClient("localhost",8000);
    private MolrWebSocketClient clientSocket=new MolrWebSocketClient("localhost",8000);

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
                                return clientSocket.receiveFlux("/getFlux",MoleExecutionEvent.class,eventsRequest).doOnError(Throwable::printStackTrace).map((tryElement)->tryElement.match(RunEvents.MissionException::new, Function.identity()));
                            }
                            @Override
                            public Mono<MoleExecutionCommandResponse> instruct(MoleExecutionCommand command) {
                                command.setMissionId(missionExecutionId);
                                return clientSocket.receiveMono("/instruct",MoleExecutionCommandResponse.class,command).doOnError(Throwable::printStackTrace).map((tryElement)->tryElement.match(CommandResponse.CommandResponseFailure::new, Function.identity()));
                            }
                        }));
    }
}
