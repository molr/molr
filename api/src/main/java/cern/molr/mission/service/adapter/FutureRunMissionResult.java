package cern.molr.mission.service.adapter;

import java.util.concurrent.CompletableFuture;

/**
 * It adapts {@link CompletableFuture < O>}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public interface FutureRunMissionResult<O> {
    O get() throws Exception;
}
