/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.type.either;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Type to construct "right" value of {@link Either}
 *
 * @param <Left>
 * @param <Right>
 *
 * @author nachivpn
 * @author yassine-kr
 */
public class Right<Left, Right> implements Either<Left, Right> {

    protected Right rightValue;

    public Right(Right rightValue) {
        this.rightValue = rightValue;
    }

    @Override
    public <T> T match(Function<Left, T> leftMapper, Function<Right, T> rightMapper) {
        return rightMapper.apply(rightValue);
    }

    @Override
    public void execute(Consumer<Left> leftConsumer, Consumer<Right> rightConsumer) {
        rightConsumer.accept(rightValue);
    }

    @Override
    public <S> Either<Left, S> rightFlatMap(Function<Right, Either<Left, S>> f) {
        return f.apply(rightValue);
    }

    @Override
    public <S> Either<Left, S> rightMap(Function<Right, S> mapper) {
        return new cern.molr.commons.type.either.Right(mapper.apply(rightValue));
    }

}