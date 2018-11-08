package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.MissionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentMissionOutputCollector implements MissionOutputCollector {

    private final Logger LOGGER = LoggerFactory.getLogger(ConcurrentMissionOutputCollector.class);

    private final ReplayProcessor<MissionOutput> output = ReplayProcessor.cacheLast();

    private final Map<Block, Map<String, Object>> blockOutputs = new ConcurrentHashMap<>();

    @Override
    public void put(Block block, String name, Number value) {
        putIt(block, name, value);
    }

    private Map<String, Object> blockMap(Block block) {
        return blockOutputs.computeIfAbsent(block, b -> new ConcurrentHashMap<>());
    }

    @Override
    public void put(Block block, String name, String value) {
        putIt(block, name, value);
    }

    private void putIt(Block block, String name, Object value) {
        if (value != null) {
            blockMap(block).put(name, value);
            publish();
        } else {
            LOGGER.warn("Value for {}, variable '{}' was a null value. Nothing added.", block, name);
        }
    }

    private void publish() {
        output.onNext(MissionOutput.fromBlocks(this.blockOutputs));
    }

    @Override
    public Flux<MissionOutput> asStream() {
        return this.output;
    }


}
