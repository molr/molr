package org.molr.commons.domain;

public interface Out {

    void emit(String name, double value);

    void emit(String name, int value);

    void emit(String name, String value);
}
