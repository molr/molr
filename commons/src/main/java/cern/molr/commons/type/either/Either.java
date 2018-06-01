/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.type.either;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A simple data type for constructing a disjoint union (or sum type) of two types. 
 * Offers pattern matching on Either values & some powerful combinators to combine Either values
 *
 * An implementation of this interface contains either an {@link L} value or an {@link R} value
 * @author nachivpn
 * @author yassine-kr
 * @param <L>
 * @param <R>
 */
public interface Either<L, R>{

    /**
     * Should call the mapper function corresponding to the current value type
     * @param leftMapper
     * @param rightMapper
     * @param <T>
     * @return
     */
    <T> T match(Function<L, T> leftMapper, Function<R, T> rightMapper);

    /**
     * Should call the consumer corresponding to the current value type
     * @param leftConsumer
     * @param rightConsumer
     */
    void execute(Consumer<L> leftConsumer,Consumer<R> rightConsumer);
    


    /**
     * Should call the {@param mapper} if the current value type is right
     * @param mapper
     * @param <S>
     * @return
     */
    <S> Either<L,S> rightMap(Function<R, S> mapper);

    <S> Either<L, S> rightFlatMap(Function<R, Either<L, S>> f);
    
}
