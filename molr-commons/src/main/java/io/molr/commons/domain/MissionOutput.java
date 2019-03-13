package io.molr.commons.domain;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyMap;

public final class MissionOutput {

    private final Map<String, Map<String, Object>> blockOutputs;

    private MissionOutput(Map<String, Map<String, Object>> values) {
        /* Copying happens in the factory methods */
        this.blockOutputs = values;
    }

    public static final MissionOutput empty() {
        return fromBlockIds(emptyMap());
    }

    public static final MissionOutput fromBlockIds(Map<String, Map<String, Object>> values) {
        Objects.requireNonNull(values, "blockOutputs must not be null");

        ImmutableMap.Builder<String, Map<String, Object>> builder = ImmutableMap.builder();
        values.entrySet().forEach(entry -> builder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue())));
        return new MissionOutput(builder.build());
    }

    public static final MissionOutput fromBlocks(Map<Block, Map<String, Object>> values) {
        Objects.requireNonNull(values, "blockOutputs must not be null");

        ImmutableMap.Builder<String, Map<String, Object>> builder = ImmutableMap.builder();
        values.entrySet().forEach(entry -> builder.put(entry.getKey().id(), ImmutableMap.copyOf(entry.getValue())));
        return new MissionOutput(builder.build());
    }

    public <T> T get(Block block, Placeholder<T> placeholder) {
        return Optional.ofNullable(blockOutputs.get(block.id()))
                .map(m -> m.get(placeholder.name()))
                .map(placeholder.type()::cast)
                .orElse(null);
    }

    public Map<String, Map<String, Object>> content() {
        return this.blockOutputs;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionOutput that = (MissionOutput) o;
        return Objects.equals(blockOutputs, that.blockOutputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockOutputs);
    }

    @Override
    public String toString() {
        return "MissionOutput{" +
                "blockOutputs=" + blockOutputs +
                '}';
    }

    public String pretty() {
        StringBuilder builder = new StringBuilder();
        blockOutputs.keySet().stream().sorted().forEach(blockId -> {
            builder.append("Block id '" + blockId + "':\n");
            Map<String, Object> blockOut = blockOutputs.get(blockId);
            blockOut.keySet().stream().sorted().forEach(key -> {
                builder.append("    " + key + "=" + Objects.toString(blockOut.get(key)) + "\n");
            });
        });
        return builder.toString();
    }
}
