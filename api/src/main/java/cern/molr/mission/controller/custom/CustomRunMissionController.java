/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.controller.custom;

import java.util.concurrent.CompletableFuture;

import cern.molr.mission.Mission;
import cern.molr.mission.controller.ClientMissionController;
import cern.molr.type.Ack;

/**
 * A custom controller used for a simple control of a running {@link Mission}
 * implementation should be a layer upon {@link ClientMissionController}
 * 
 * @author nachivpn
 * @author yassine-kr
 * @param <T>
 */
public interface CustomRunMissionController<T> {

     CompletableFuture<T> getResult();
    
     //return type Ack may change later
     CompletableFuture<Ack> cancel();
}
