/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.type.either;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Type to construct "left" value for {@link Either}
 *
 * @param <Left>
 * @param <Right>
 *
 * @author nachivpn
 * @author yassine-kr
 */
public class Left<Left, Right> implements Either<Left, Right> {

    protected Left leftValue;

    public Left(Left leftValue) {
        this.leftValue = leftValue;
    }

    @Override
    public <T> T match(Function<Left, T> leftMapper, Function<Right, T> rightMapper) {
        return leftMapper.apply(leftValue);
    }

    @Override
    public void execute(Consumer<Left> leftConsumer, Consumer<Right> rightConsumer) {
        leftConsumer.accept(leftValue);
    }

    @Override
    public <S> Either<Left, S> rightFlatMap(Function<Right, Either<Left, S>> f) {
        return new cern.molr.commons.type.either.Left(leftValue);
    }

    @Override
    public <S> Either<Left, S> rightMap(Function<Right, S> mapper) {
        return new cern.molr.commons.type.either.Left(leftValue);
    }

}

