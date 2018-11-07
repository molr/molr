package org.molr.commons.domain;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyMap;

public class MissionInput implements In {

    private final Map<String, Object> values;

    private MissionInput(Map<String, Object> values) {
        this.values = ImmutableMap.copyOf(values);
    }

    public static final MissionInput empty() {
        return from(emptyMap());
    }

    public static final MissionInput from(Map<String, Object> values) {
        return new MissionInput(values);
    }

    @Override
    public <T> T get(Placeholder<T> placeholder) {
        return Optional.ofNullable(values.get(placeholder.name()))
                .map(placeholder.type()::cast)
                .orElse(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionInput that = (MissionInput) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return "MissionInput{" +
                "values=" + values +
                '}';
    }
}
