/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.trye;

import cern.molr.type.Try;
import cern.molr.type.either.Left;

/**
 * Refer to {@link Try}
 * 
 * @author nachivpn 
 * @param <T>
 */
public class Failure<T> extends Left<Throwable, T> implements Try<T>{
   
    public Failure() {
        super(null);
    }
    
    /**
     * @param l
     */
    public Failure(Throwable l) {
        super(l);
    }

}
