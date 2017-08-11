/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.type.trye;

import cern.molr.type.either.Left;

/**
 * Refer to {@link Try}
 * 
 * @author nachivpn 
 * @param <T>
 */
public class Failure<T> extends Left<Exception, T> implements Try<T>{

    /**
     * @param l
     */
    public Failure(Exception l) {
        super(l);
    }


}
