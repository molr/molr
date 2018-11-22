package org.molr.commons.domain;

public interface Out {

    void emit(String name, Number value);

    void emit(String name, String value);

    <T> void emit(Placeholder<T> placeholder, T value);

}
