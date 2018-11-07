package org.molr.commons.domain;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Placeholder<T> {

    private final Class<T> type;
    private final String name;

    Placeholder(Class<T> type, String name) {
        this.type = requireNonNull(type, "type must not be null");
        this.name = requireNonNull(name, "name must not be null");
    }

    public static final Placeholder<Number> number(String name) {
        return new Placeholder<>(Number.class, name);
    }

    public static final Placeholder<String> string(String name) {
        return new Placeholder<>(String.class, name);
    }

    public Class<T> type() {
        return this.type;
    }

    public String name() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Placeholder<?> that = (Placeholder<?>) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public String toString() {
        return "Placeholder{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}