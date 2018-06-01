/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.type.trye;

import cern.molr.commons.type.trye.Try;
import cern.molr.commons.type.either.Right;

/**
 * Refer to {@link Try}
 * 
 * @author nachivpn
 * @author yassine-kr
 * @param <T>
 */
public class Success<T> extends Right<Throwable, T> implements Try<T>{


    public Success() {
        super(null);
    }
    

    public Success(T successValue) {
        super(successValue);
    }

}
