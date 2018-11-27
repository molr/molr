package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.MissionOutput;
import org.molr.commons.domain.Placeholder;
import reactor.core.publisher.Flux;

public interface MissionOutputCollector {

    void put(Block block, String name, Number value);

    void put(Block block, String name, String value);

    <T> void put(Block block, Placeholder<T> placeholder, T value);

    Flux<MissionOutput> asStream();
}
