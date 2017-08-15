/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.serviceimpl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

import cern.molr.exception.UnsupportedOutputTypeException;
import cern.molr.mission.run.RunMissionController;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.mission.step.StepMissionController;
import cern.molr.rest.bean.MissionExecutionResponseBean;
import cern.molr.rest.request.MissionCancelRequest;
import cern.molr.rest.request.MissionExecutionRequest;
import cern.molr.rest.request.MissionResultRequest;
import cern.molr.rest.result.MissionCancelResponse;
import cern.molr.rest.result.MissionExecutionResponse;
import cern.molr.rest.result.MissionIntegerResponse;
import cern.molr.rest.result.MissionXResponse;
import cern.molr.type.Ack;
import cern.molr.web.MolrWebClient;

/**
 * Implementation used by the operator to interact with the server
 * 
 * @author nachivpn
 */
public class MissionExecutionServiceImpl implements MissionExecutionService{

    MolrWebClient client = new MolrWebClient("localhost",8080);

    @Override
    public <I, O> CompletableFuture<RunMissionController<O>> runToCompletion(String missionDefnClassName, I args, Class<I> cI, Class<O> cO) throws UnsupportedOutputTypeException {
        Class<? extends MissionXResponse<?>> resultResponseType = getMissionResultResponseType(cO);
        MissionExecutionRequest<I> execRequest = new MissionExecutionRequest<>(missionDefnClassName, args);
        return client.post("/mission", MissionExecutionRequest.class, execRequest, MissionExecutionResponse.class)
                .thenApply(tryResp -> tryResp.match(
                        (Throwable e) -> {throw new CompletionException(e);}, 
                        (MissionExecutionResponseBean resp) -> resp.getMissionExecutionId()))
                .<RunMissionController<O>> thenApply(missionExecutionId -> new RunMissionController<O>() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public CompletableFuture<O> getResult() {
                            MissionResultRequest resultRequest = new MissionResultRequest(missionExecutionId);
                                return client.post("/result", MissionResultRequest.class, resultRequest, resultResponseType)
                                        .thenApply(resultResponse -> resultResponse.match(
                                                (Throwable e) -> {throw new CompletionException(e);}, 
                                                resp -> (O) resp));
                    }

                    @Override
                    public CompletableFuture<Ack> cancel() {
                        MissionCancelRequest cancelRequest = new MissionCancelRequest(missionExecutionId);
                        return client.post("/cancel", MissionCancelRequest.class, cancelRequest, MissionCancelResponse.class)
                                .thenApply(cancelResponse -> cancelResponse.match(
                                        (Throwable e) -> {throw new CompletionException(e);}, 
                                        //Return the result as it is
                                        Function.identity()
                                        ));
                    }
                });

    }

    @Override
    public <I, O> CompletableFuture<StepMissionController<O>> step(String missionDefnClassName, I args) {
        return null;
    }

    private Class<? extends MissionXResponse<?>> getMissionResultResponseType(Class<?> c) throws UnsupportedOutputTypeException {
        if(c.equals(Integer.class))
            return MissionIntegerResponse.class;
        else
            throw new UnsupportedOutputTypeException("Error occured on client: " + c.getName() + " is not supported yet!");
    }


}
