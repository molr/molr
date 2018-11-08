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

    public static final <T> Placeholder<T> __do_not_use_externally__create__(Class<T> type, String name) {
        return new Placeholder<>(type, name);
    }

    public static final Placeholder<Double> aDouble(String name) {
        return new Placeholder<>(Double.class, name);
    }

    public static final Placeholder<Boolean> aBoolean(String name) {
        return new Placeholder<>(Boolean.class, name);
    }

    /* aLong does not work at the moment ... the transport over json always converts into integers
    when numbers fit into integers and then the case fails .. so for the moment we avoid it ;-)*/

    public static final Placeholder<Integer> anInteger(String name) {
        return new Placeholder<>(Integer.class, name);
    }

    public static final Placeholder<String> aString(String name) {
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
