package cern.molr.client.serviceimpl.custom.adapter;

import cern.molr.mission.service.custom.adapter.FutureRunMissionResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * It adapts {@link CompletableFuture < O>}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public class FutureRunMissionResultImpl<O> implements FutureRunMissionResult<O> {

    private CompletableFuture<O> futureResult;

    public FutureRunMissionResultImpl(CompletableFuture<O> futureResult) {
        this.futureResult = futureResult;
    }

    @Override
    public O get() throws Exception {
        try{
            return futureResult.get();
        }catch(ExecutionException e){
            //TODO to implement
            throw new Exception();
        }
    }
}
