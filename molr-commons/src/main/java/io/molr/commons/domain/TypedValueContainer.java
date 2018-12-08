package io.molr.commons.domain;

public interface TypedValueContainer {

    <T> T get(Placeholder<T> placeholder);

}
