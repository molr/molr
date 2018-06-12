/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.type.either;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Type to construct "right" value of {@link Either}
 *
 * @param <L>
 * @param <R>
 *
 * @author nachivpn
 * @author yassine-kr
 */
public class Right<L, R> implements Either<L, R> {

    protected final R rightValue;

    public Right(R rightValue) {
        this.rightValue = rightValue;
    }

    @Override
    public <T> T match(Function<L, T> leftMapper, Function<R, T> rightMapper) {
        return rightMapper.apply(rightValue);
    }

    @Override
    public void execute(Consumer<L> leftConsumer, Consumer<R> rightConsumer) {
        rightConsumer.accept(rightValue);
    }

    @Override
    public <S> Either<L, S> rightFlatMap(Function<R, Either<L, S>> f) {
        return f.apply(rightValue);
    }

    @Override
    public <S> Either<L, S> rightMap(Function<R, S> mapper) {
        return new Right<>(mapper.apply(rightValue));
    }

}