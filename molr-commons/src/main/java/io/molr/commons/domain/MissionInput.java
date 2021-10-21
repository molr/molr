package io.molr.commons.domain;

import static java.util.Collections.emptyMap;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public final class MissionInput extends AbstractTypedValueContainer implements In {

    private MissionInput(Map<String, Object> values) {
        super(values);
    }

    public MissionInput addOrOverride(Map<String, Object> values) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        values().forEach((key, val)->{
        	if(!values.containsKey(key)) {
        		builder.put(key, val);
        	}
        });
        builder.putAll(values);
        return MissionInput.from(builder.build());
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
