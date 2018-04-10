/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.trye;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonDeserialize(as = TryResponseFailure.class)
@JsonSerialize(using=TryResponseFailureSerializer.class)
public class TryResponseFailure<T> extends Failure<T> implements TryResponse<T>{

    public TryResponseFailure() {
        super(null);
    }
    
    /**
     * @param l
     */
    public TryResponseFailure(Throwable l) {
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
