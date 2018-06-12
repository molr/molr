/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.type.trye;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author ?
 * @author yassine-kr
 */
public class TryResponseSuccess<T> extends Success<T> implements TryResponse<T> {

    public TryResponseSuccess(T result) {
        super(result);
    }

    @Override
    @JsonIgnore
    public Throwable getThrowable() {
        return null;
    }

    @Override
    @JsonProperty("success")
    public T getResult() {
        return this.rightValue;
    }

}
