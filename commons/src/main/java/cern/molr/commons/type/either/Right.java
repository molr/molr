/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.type.either;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Type to construct "right" value of {@link Either}
 *
 * @param <RL>
 * @param <RR>
 *
 * @author nachivpn
 * @author yassine-kr
 */
public class Right<RL, RR> implements Either<RL, RR> {

    protected RR rightValue;

    public Right(RR rightValue) {
        this.rightValue = rightValue;
    }

    @Override
    public <T> T match(Function<RL, T> leftMapper, Function<RR, T> rightMapper) {
        return rightMapper.apply(rightValue);
    }

    @Override
    public void execute(Consumer<RL> leftConsumer, Consumer<RR> rightConsumer) {
        rightConsumer.accept(rightValue);
    }

    @Override
    public <S> Either<RL, S> rightFlatMap(Function<RR, Either<RL, S>> f) {
        return f.apply(rightValue);
    }

    @Override
    public <S> Either<RL, S> rightMap(Function<RR, S> mapper) {
        return new Right<>(mapper.apply(rightValue));
    }

}