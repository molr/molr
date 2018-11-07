package org.molr.commons.domain;

public interface MissionOutputCollector {

    void put(Block block, String name, Number value);

    void put(Block block, String name, String value);

}
