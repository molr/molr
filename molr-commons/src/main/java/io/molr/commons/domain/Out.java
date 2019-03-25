package io.molr.commons.domain;

import java.util.Collection;

public interface Out {

    void emit(String name, Number value);

    void emit(String name, String value);

    <T> void emit(Placeholder<T> placeholder, T value);

    <T> void emit(Placeholder<T> placeholder, Collection<T> value);
}
