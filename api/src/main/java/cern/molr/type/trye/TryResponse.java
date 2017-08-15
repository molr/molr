/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.type.trye;

import cern.molr.type.trye.Try;

public interface TryResponse<T> extends Try<T>{
    public Exception getException();
    public void setException(Exception e);
    public T getResult();
    public void setResult(T r);
}
