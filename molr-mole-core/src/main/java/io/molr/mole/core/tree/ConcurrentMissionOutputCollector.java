package io.molr.mole.core.tree;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MissionOutput;
import io.molr.commons.domain.Placeholder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ConcurrentMissionOutputCollector implements MissionOutputCollector {

    private final Logger LOGGER = LoggerFactory.getLogger(ConcurrentMissionOutputCollector.class);

    //private final Scheduler scheduler = Schedulers.newBoundedElastic(Schedulers.DEFAULT_POOL_SIZE,
    	//	Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE, "output-scheduler");
    private final Scheduler scheduler = Schedulers.newSingle("output-collector", true);
    /*
     * TODO instead of disposing scheduler we may use permanent shared thread pool
     */
    private final Flux<MissionOutput> outputStream;
    private final Sinks.Many<MissionOutput> outputSink;

    private final Map<Block, Map<String, Object>> blockOutputs = new ConcurrentHashMap<>();

    public ConcurrentMissionOutputCollector() {
        outputSink = Sinks.many().replay().latest();
        outputStream = outputSink.asFlux().publishOn(scheduler).cache(1);
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
        outputSink.tryEmitNext(MissionOutput.fromBlocks(this.blockOutputs));
    }

    @Override
    public Flux<MissionOutput> asStream() {
        return this.outputStream;
    }

    @Override
    public void onComplete() {
    	outputSink.tryEmitComplete();
    }


}
