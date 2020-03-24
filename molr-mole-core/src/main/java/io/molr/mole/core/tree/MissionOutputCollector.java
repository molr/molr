package io.molr.mole.core.tree;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.Placeholder;
import reactor.core.publisher.Flux;

public interface MissionOutputCollector {

    void put(Block block, String name, Number value);

    void put(Block block, String name, String value);

    <T> void put(Block block, Placeholder<T> placeholder, T value);

    Flux<MissionOutput> asStream();
    
    void onComplete();
}
