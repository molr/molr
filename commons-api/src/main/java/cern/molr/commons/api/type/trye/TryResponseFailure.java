/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.type.trye;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yassine-kr
 */
public class TryResponseFailure<T> extends Failure<T> implements TryResponse<T> {

    public TryResponseFailure(Throwable throwable) {
        super(throwable);
    }

    @Override
    @JsonProperty("throwable")
    public Throwable getThrowable() {
        return leftValue;
    }

    @Override
    @JsonIgnore
    public T getResult() {
        return null;
    }
}
