package io.molr.mole.core.tree;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.Placeholder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ConcurrentMissionOutputCollector implements MissionOutputCollector {

    private final Logger LOGGER = LoggerFactory.getLogger(ConcurrentMissionOutputCollector.class);

    private final Scheduler scheduler = Schedulers.newSingle("output-collector", true);
    /*
     * TODO instead of disposing scheduler we may use permanent shared thread pool
     */
    private final Flux<MissionOutput> outputStream;
    private final FluxSink<MissionOutput> outputSink;

    private final Map<Block, Map<String, Object>> blockOutputs = new ConcurrentHashMap<>();

    public ConcurrentMissionOutputCollector() {
        ReplayProcessor<MissionOutput> outputProcessor = ReplayProcessor.cacheLast();
        outputSink = outputProcessor.sink();
        outputStream = outputProcessor.publishOn(scheduler).cache(1).doFinally(signal -> {scheduler.dispose();});
    }
    
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
        outputSink.next(MissionOutput.fromBlocks(this.blockOutputs));
    }

    @Override
    public Flux<MissionOutput> asStream() {
        return this.outputStream;
    }

    @Override
    public void onComplete() {
        outputSink.complete();        
    }


}
