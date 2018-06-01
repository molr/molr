/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.type.trye;

import cern.molr.commons.type.either.Either;

/**
 * {@link Try} is used to return an "exceptional" result.
 * A Failure (which contains the exception) is returned when the result computation failed with an exception.
 * A Success (which contains the result) is returned when the result is available.
 * NOTE: This type is very useful to propagate exceptions remotely in a robust manner.
 * @param <T>
 *
 * @author nachivpn
 * @author yassine-kr
 */
public interface Try<T> extends Either<Throwable,T>{
}
