package cern.molr.client.serviceimpl.adapter;

import cern.molr.mission.service.adapter.FutureRunMissionCancelResult;
import cern.molr.type.Ack;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * It adapts {@link CompletableFuture <{@link Ack}>}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public class FutureRunMissionCancelResultImpl implements FutureRunMissionCancelResult {

    private CompletableFuture<Ack> futureCancelResult;

    public FutureRunMissionCancelResultImpl(CompletableFuture<Ack> futureCancelResult) {
        this.futureCancelResult = futureCancelResult;
    }

    @Override
    public Ack get() throws Exception {
        try{
            return futureCancelResult.get();
        }catch(ExecutionException e){
            throw (Exception) (Class.forName(e.getCause().getCause().getMessage()).getConstructor(String.class)).newInstance(e.getCause().getMessage());
        }

    }
}
