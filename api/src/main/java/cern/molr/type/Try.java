/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.type;

import cern.molr.type.either.Either;

/**
 * {@link Try} is used to an return an "exceptional" result. 
 * A {@link Failure} (which contains the exception) is returned when the result computation failed with an exception.
 * A {@link Success} (which contains the result) is returned when the result is available.
 * NOTE: This type is very useful to propagate exceptions remotely in a robust manner.
 * 
 * @author nachivpn 
 * @param <T>
 */
public interface Try<T> extends Either<Throwable,T>{
}
