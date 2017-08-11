/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.type.either;

import java.util.function.Function;

/**
 * A simple data type for constructing a disjoint union (or sum type) of two types. 
 * Offers pattern matching on Either values & some powerful combinators to combine Either values
 * 
 * @author nachivpn 
 * @param <L>
 * @param <R>
 */
public interface Either<L, R>{

    public <T> T match(Function<L, T> a, Function<R, T> b);
    
    public <S> Either<L, S> bind(Function<R, Either<L, S>> f);
    
    public <S> Either<L,S> fmap(Function<R, S> f);
    
}
