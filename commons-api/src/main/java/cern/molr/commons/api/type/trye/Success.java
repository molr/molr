/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.type.trye;

import cern.molr.commons.api.type.either.Right;

/**
 * Refer to {@link Try}
 *
 * @param <T>
 *
 * @author nachivpn
 * @author yassine-kr
 */
public class Success<T> extends Right<Throwable, T> implements Try<T> {

    public Success(T successValue) {
        super(successValue);
    }

}
