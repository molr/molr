package cern.molr.client.serviceimpl.adapter;

import cern.molr.mission.service.adapter.FutureRunMissionResult;

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
            throw (Exception) (Class.forName(e.getCause().getCause().getMessage()).getConstructor(String.class)).newInstance(e.getCause().getMessage());
        }
    }
}
