/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.type.either;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Type to construct "left" value for {@link Either}
 * 
 * @author nachivpn
 * @author yassine-kr
 * @param <LL>
 * @param <LR>
 */
public class Left<LL, LR> implements Either<LL, LR>{

    protected LL l;
    
    public Left(LL l) {
        this.l = l;
    }

    @Override
    public <T> T match(Function<LL, T> leftMapper, Function<LR, T> rightMapper) {
        return leftMapper.apply(l);
    }

    @Override
    public void execute(Consumer<LL> leftConsumer, Consumer<LR> rightConsumer) {
        leftConsumer.accept(l);
    }

    @Override
    public <S> Either<LL, S> rightFlatMap(Function<LR, Either<LL, S>> f) {
        return new Left<>(l);
    }

    @Override
    public <S> Either<LL, S> rightMap(Function<LR, S> mapper) {
        return new Left<>(l);
    }

}

