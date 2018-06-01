/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.type.either;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Type to construct "right" value of {@link Either}
 * 
 * @author nachivpn
 * @author yassine-kr
 * @param <RL>
 * @param <RR>
 */
public class Right<RL,RR> implements Either<RL, RR>{

    protected RR r;
    
    public Right(RR r) {
        this.r = r;
    }

    @Override
    public <T> T match(Function<RL, T> leftMapper, Function<RR, T> rightMapper) {
        return rightMapper.apply(r);
    }

    @Override
    public void execute(Consumer<RL> leftConsumer, Consumer<RR> rightConsumer) {
        rightConsumer.accept(r);
    }

    @Override
    public <S> Either<RL, S> rightFlatMap(Function<RR, Either<RL, S>> f) {
        return f.apply(r);
    }

    @Override
    public <S> Either<RL, S> rightMap(Function<RR, S> mapper) {
        return new Right<>(mapper.apply(r));
    }
    
}