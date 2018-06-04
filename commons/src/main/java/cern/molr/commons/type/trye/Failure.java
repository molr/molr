/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.type.trye;

import cern.molr.commons.type.either.Left;

/**
 * Refer to {@link Try}
 *
 * @param <T>
 *
 * @author nachivpn
 * @author yassine-kr
 */
public class Failure<T> extends Left<Throwable, T> implements Try<T> {

    public Failure() {
        super(null);
    }

    public Failure(Throwable throwable) {
        super(throwable);
    }

}
