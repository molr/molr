/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.run;

import java.util.concurrent.CompletableFuture;

import cern.molr.mission.Mission;
import cern.molr.type.Ack;

/**
 * Controller used to control a running {@link Mission}
 * 
 * @author nachivpn 
 * @param <T>
 */
public interface RunMissionController<T> {

     CompletableFuture<T> getResult();
    
     //return type Ack may change later
     CompletableFuture<Ack> cancel();
}
