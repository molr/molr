/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.trye;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author ?
 * @author yassine-kr
 */
public class TryResponseSuccess<T> extends Success<T> implements TryResponse<T>{

    public TryResponseSuccess() {
        super(null);
    }
    
    /**
     * @param r
     */
    public TryResponseSuccess(T r) {
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
        return this.r;
    }

    @Override
    public void setResult(T r) {
        this.r = r;
    }

}
