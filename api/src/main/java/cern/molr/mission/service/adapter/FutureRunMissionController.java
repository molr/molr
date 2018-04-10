package cern.molr.mission.service.adapter;


import java.util.concurrent.CompletableFuture;

/**
 * It adapts {@link CompletableFuture < {@link cern.molr.mission.run.RunMissionController}<O>>}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public interface FutureRunMissionController<O> {
    RunMissionController<O> get()throws Exception;
}
