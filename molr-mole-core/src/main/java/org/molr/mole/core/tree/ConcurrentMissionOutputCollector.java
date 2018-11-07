package org.molr.mole.core.tree;

import org.molr.commons.domain.Block;
import org.molr.commons.domain.MissionOutputCollector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentMissionOutputCollector implements MissionOutputCollector {

    private final Map<Block, Map<String, Object>> outputs = new ConcurrentHashMap<>();

    @Override
    public void put(Block block, String name, Number value) {
        blockMap(block).put(name, value);
    }

    private Map<String, Object> blockMap(Block block) {
        return outputs.computeIfAbsent(block, b -> new ConcurrentHashMap<>());
    }

    @Override
    public void put(Block block, String name, String value) {
        blockMap(block).put(name, value);
    }


}
