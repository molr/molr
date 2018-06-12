/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.type.either;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Type to construct "left" value for {@link Either}
 *
 * @param <L>
 * @param <R>
 *
 * @author nachivpn
 * @author yassine-kr
 */
public class Left<L, R> implements Either<L, R> {

    protected final L leftValue;

    public Left(L leftValue) {
        this.leftValue = leftValue;
    }

    @Override
    public <T> T match(Function<L, T> leftMapper, Function<R, T> rightMapper) {
        return leftMapper.apply(leftValue);
    }

    @Override
    public void execute(Consumer<L> leftConsumer, Consumer<R> rightConsumer) {
        leftConsumer.accept(leftValue);
    }

    @Override
    public <S> Either<L, S> rightFlatMap(Function<R, Either<L, S>> f) {
        return new Left<>(leftValue);
    }

    @Override
    public <S> Either<L, S> rightMap(Function<R, S> mapper) {
        return new Left<>(leftValue);
    }

}

