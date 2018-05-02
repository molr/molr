/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.serviceimpl;

import cern.molr.commons.response.*;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.exception.UnsupportedOutputTypeException;
import cern.molr.mission.run.RunMissionController;
import cern.molr.mission.run.RunMissionControllerNew;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.mission.service.MissionExecutionServiceNew;
import cern.molr.mission.step.StepMissionController;
import cern.molr.mole.spawner.run.RunCommands;
import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionResponseCommand;
import cern.molr.server.request.MissionCancelRequest;
import cern.molr.server.request.MissionEventsRequest;
import cern.molr.server.request.MissionExecutionRequest;
import cern.molr.server.request.MissionResultRequest;
import cern.molr.type.Ack;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

/**
 * Implementation used by the operator to interact with the server
 * 
 * @author yassine
 */
public class MissionExecutionServiceImplNew implements MissionExecutionServiceNew {

    private MolrWebClient client = new MolrWebClient("localhost",8000);
    private MolrWebSocketClient clientSocket=new MolrWebSocketClient("localhost",8000);

    @Override
    public <I> Mono<RunMissionControllerNew> instantiate(String missionDefnClassName, I args) {
        MissionExecutionRequest<I> execRequest = new MissionExecutionRequest<>(missionDefnClassName, args);
        return Mono.fromFuture(client.post("/instantiate", MissionExecutionRequest.class, execRequest, MissionExecutionResponse.class)
                .thenApply(tryResp -> tryResp.match(
                        (Throwable e) -> {
                            throw new CompletionException(e);
                            }, MissionExecutionResponseBean::getMissionExecutionId))
                .thenApply(missionExecutionId -> new RunMissionControllerNew() {
                            @Override
                            public Flux<MoleExecutionEvent> getFlux() {
                                MissionEventsRequest eventsRequest=new MissionEventsRequest(missionExecutionId);
                                return clientSocket.receiveFlux("/getFlux",MoleExecutionEvent.class,eventsRequest).doOnError(Throwable::printStackTrace);
                            }
                            @Override
                            public Mono<MoleExecutionResponseCommand> instruct(MoleExecutionCommand command) {
                                command.setId(missionExecutionId);
                                return clientSocket.receiveMono("/instruct",MoleExecutionResponseCommand.class,command);
                            }
                        }));
    }
}
