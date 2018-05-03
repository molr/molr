/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.serviceimpl;

import cern.molr.commons.response.MissionExecutionResponse;
import cern.molr.commons.response.MissionExecutionResponseBean;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.web.MolrWebSocketClient;
import cern.molr.mission.run.RunMissionControllerNew;
import cern.molr.mission.service.MissionExecutionServiceNew;
import cern.molr.mole.spawner.debug.ResponseCommand;
import cern.molr.mole.spawner.run.RunEvents;
import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionResponseCommand;
import cern.molr.server.request.MissionEventsRequest;
import cern.molr.server.request.MissionExecutionRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
                                return clientSocket.receiveFlux("/getFlux",MoleExecutionEvent.class,eventsRequest).doOnError(Throwable::printStackTrace).map((tryElement)->tryElement.match(RunEvents.MissionException::new, Function.identity()));
                            }
                            @Override
                            public Mono<MoleExecutionResponseCommand> instruct(MoleExecutionCommand command) {
                                command.setId(missionExecutionId);
                                return clientSocket.receiveMono("/instruct",MoleExecutionResponseCommand.class,command).doOnError(Throwable::printStackTrace).map((tryElement)->tryElement.match(ResponseCommand.ResponseCommandFailure::new, Function.identity()));
                            }
                        }));
    }
}
