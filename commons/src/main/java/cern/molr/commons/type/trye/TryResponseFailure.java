/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.type.trye;

/**
 * @author ?
 * @author yassine-kr
 */
public class TryResponseFailure<T> extends Failure<T> implements TryResponse<T> {

    public TryResponseFailure() {
        super(null);
    }

    public TryResponseFailure(Throwable throwable) {
        super(throwable);
    }

    @Override
    public Throwable getThrowable() {
        return leftValue;
    }

    @Override
    public void setThrowable(Throwable throwable) {
        this.leftValue = throwable;
    }

    @Override
    public T getResult() {
        return null;
    }

    @Override
    public void setResult(T result) {
        return;
    }

}
