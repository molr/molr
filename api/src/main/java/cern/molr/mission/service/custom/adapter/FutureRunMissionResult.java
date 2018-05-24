package cern.molr.mission.service.custom.adapter;

import java.util.concurrent.CompletableFuture;

/**
 * It adapts {@link CompletableFuture < O>}, used to throw meaningful exceptions
 *
 * @author yassine-kr
 */
public interface FutureRunMissionResult<O> {
    O get() throws Exception;
}
