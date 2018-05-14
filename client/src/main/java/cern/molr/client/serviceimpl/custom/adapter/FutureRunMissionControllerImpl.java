package cern.molr.client.serviceimpl.custom.adapter;

import cern.molr.mission.controller.custom.CustomRunMissionController;
import cern.molr.mission.service.custom.adapter.FutureRunMissionController;
import cern.molr.mission.service.custom.adapter.RunMissionControllerAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * It adapts {@link CompletableFuture <{@link CustomRunMissionController }<O>>}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public class FutureRunMissionControllerImpl<O> implements FutureRunMissionController<O> {

    private CompletableFuture<CustomRunMissionController<O>> futureController;

    public FutureRunMissionControllerImpl(CompletableFuture<CustomRunMissionController<O>> futureController) {
        this.futureController = futureController;
    }

    @Override
    public RunMissionControllerAdapter<O> get() throws Exception {

        try {
            return new RunMissionControllerAdapterImpl<O>(futureController.get());
        }catch(ExecutionException e){
            //TODO to implement
            throw new Exception();
        }

    }
}
