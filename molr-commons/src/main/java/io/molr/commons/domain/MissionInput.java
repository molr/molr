package io.molr.commons.domain;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static java.util.Collections.emptyMap;

public final class MissionInput extends AbstractTypedValueContainer implements In {

    private MissionInput(Map<String, Object> values) {
        super(values);
    }

    public MissionInput and(Map<String, Object> values) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(values());
        builder.putAll(values);
        return MissionInput.from(builder.build());
    }

    public MissionInput and(String key, Object value) {
        return and(ImmutableMap.of(key, value));
    }

    public static final MissionInput empty() {
        return from(emptyMap());
    }

    public static final MissionInput from(Map<String, Object> values) {
        return new MissionInput(values);
    }


}
