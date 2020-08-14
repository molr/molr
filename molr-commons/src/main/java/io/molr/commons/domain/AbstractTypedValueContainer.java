package io.molr.commons.domain;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractTypedValueContainer implements TypedValueContainer {

    private final Map<String, Object> values;

    AbstractTypedValueContainer(Map<String, Object> values) {
        this.values = ImmutableMap.copyOf(values);
    }

    @Override
    public <T> T get(Placeholder<T> placeholder) {
        return Optional.ofNullable(values.get(placeholder.name()))
                .map(placeholder.type()::cast)
                .orElse(null);
    }

    protected Map<String, Object> values() {
        return this.values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractTypedValueContainer that = (AbstractTypedValueContainer) o;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "values=" + values +
                '}';
    }
}
