/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.type.trye;

import cern.molr.type.either.Right;

/**
 * Refer to {@link Try}
 * 
 * @author nachivpn 
 * @param <T>
 */
public class Success<T> extends Right<Throwable, T> implements TryResponse<T>{

    /**
     * @param r
     */
    public Success(T r) {
        super(r);
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    @Override
    public void setThrowable(Throwable e) {
        return;
    }

    @Override
    public T getResult() {
        return r;
    }

    @Override
    public void setResult(T r) {
        this.r = r;
    }


}
