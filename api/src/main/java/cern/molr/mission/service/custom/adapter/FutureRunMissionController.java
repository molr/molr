package cern.molr.mission.service.custom.adapter;


import cern.molr.mission.controller.custom.CustomRunMissionController;

import java.util.concurrent.CompletableFuture;

/**
 * It adapts {@link CompletableFuture < {@link CustomRunMissionController }<O>>}, used to throw meaningful exceptions
 *
 * @author yassine-kr
 */
public interface FutureRunMissionController<O> {
    RunMissionControllerAdapter<O> get()throws Exception;
}
