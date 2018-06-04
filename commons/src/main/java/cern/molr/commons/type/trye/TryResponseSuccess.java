/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.type.trye;

/**
 * @author ?
 * @author yassine-kr
 */
public class TryResponseSuccess<T> extends Success<T> implements TryResponse<T> {

    public TryResponseSuccess() {
        super(null);
    }

    public TryResponseSuccess(T result) {
        super(result);
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    @Override
    public void setThrowable(Throwable throwable) {
        return;
    }

    @Override
    public T getResult() {
        return this.rightValue;
    }

    @Override
    public void setResult(T result) {
        this.rightValue = result;
    }

}
