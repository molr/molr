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
public class Failure<T> extends Left<Throwable, T> implements TryResponse<T>{
    
    /**
     * @param l
     */
    public Failure(Throwable l) {
        super(l);
    }

    @Override
    public Throwable getThrowable() {
        return l;
    }

    @Override
    public void setThrowable(Throwable e) {
        this.l = e;
    }

    @Override
    public T getResult() {
        return null;
    }

    @Override
    public void setResult(T r) {
        return;
    }


}
