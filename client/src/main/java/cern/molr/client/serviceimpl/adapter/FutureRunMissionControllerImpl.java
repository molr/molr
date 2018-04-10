package cern.molr.client.serviceimpl.adapter;

import cern.molr.mission.service.adapter.FutureRunMissionController;
import cern.molr.mission.service.adapter.RunMissionController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * It adapts {@link CompletableFuture < {@link cern.molr.mission.run.RunMissionController}<O>>}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public class FutureRunMissionControllerImpl<O> implements FutureRunMissionController<O> {

    private CompletableFuture<cern.molr.mission.run.RunMissionController<O>> futureController;

    public FutureRunMissionControllerImpl(CompletableFuture<cern.molr.mission.run.RunMissionController<O>> futureController) {
        this.futureController = futureController;
    }

    @Override
    public RunMissionController<O> get() throws Exception {

        try {
            return new RunMissionControllerImpl<O>(futureController.get());
        }catch(ExecutionException e){
            throw (Exception) (Class.forName(e.getCause().getCause().getMessage()).getConstructor(String.class)).newInstance(e.getCause().getMessage());
        }

    }
}
