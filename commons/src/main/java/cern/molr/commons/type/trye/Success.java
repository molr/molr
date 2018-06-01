/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.trye;

import cern.molr.type.Try;
import cern.molr.type.either.Right;

/**
 * Refer to {@link Try}
 * 
 * @author nachivpn 
 * @param <T>
 */
public class Success<T> extends Right<Throwable, T> implements Try<T>{

    /**
     * @param r
     */
    public Success() {
        super(null);
    }
    
    /**
     * @param r
     */
    public Success(T r) {
        super(r);
    }

}
