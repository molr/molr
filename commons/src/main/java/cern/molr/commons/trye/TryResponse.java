/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.trye;

import cern.molr.type.Try;

/**
 * {@link TryResponse} is bean-ified Try
 * It offers getters and setters (which may return null or be ineffective respectively),
 * but is easy to serialize and might be preferred by some for programming
 * @author nachivpn 
 * @param <T>
 */
public interface TryResponse<T> extends Try<T>{
    public Throwable getThrowable();
    public void setThrowable(Throwable e);
    public T getResult();
    public void setResult(T r);
}
