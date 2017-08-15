/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.type.trye;

import cern.molr.type.trye.Try;

public interface TryResponse<T> extends Try<T>{
    public Throwable getThrowable();
    public void setThrowable(Throwable e);
    public T getResult();
    public void setResult(T r);
}
