/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.run;

import java.util.concurrent.CompletableFuture;

/**
 * A generic {@link RunSession}, encapsulates the information of a currently running {@link Mission}
 *
 * @author nachivpn
 */
public interface RunSession<O>{

    CompletableFuture<O> getResult();
    
}
