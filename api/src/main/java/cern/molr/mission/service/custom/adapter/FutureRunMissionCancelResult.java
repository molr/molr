package cern.molr.mission.service.custom.adapter;

import cern.molr.type.Ack;

import java.util.concurrent.CompletableFuture;

/**
 * It adapts {@link CompletableFuture < {@link Ack} >}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public interface FutureRunMissionCancelResult {
    Ack get() throws Exception;
}
