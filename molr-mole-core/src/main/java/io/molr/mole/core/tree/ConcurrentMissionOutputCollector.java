package io.molr.mole.core.tree;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.Placeholder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentMissionOutputCollector implements MissionOutputCollector {

    private final Logger LOGGER = LoggerFactory.getLogger(ConcurrentMissionOutputCollector.class);

    private final ReplayProcessor<MissionOutput> outputSink = ReplayProcessor.cacheLast();
    private final Scheduler scheduler = Schedulers.newSingle("output-collector", true);
    /*
     * TODO instead of disposing scheduler we may use permanent shared thread pool
     */
    private final Flux<MissionOutput> outputStream = outputSink.publishOn(scheduler).cache(1).doFinally(signal -> {scheduler.dispose();});

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

    @Override
    public <T> void put(Block block, Placeholder<T> placeholder, T value) {
        putIt(block, placeholder.name(), value);
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
        outputSink.onNext(MissionOutput.fromBlocks(this.blockOutputs));
    }

    @Override
    public Flux<MissionOutput> asStream() {
        return this.outputStream;
    }

    @Override
    public void onComplete() {
        outputSink.onComplete();        
    }


}
