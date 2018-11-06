package org.molr.commons.domain;

/**
 * Represents the input of a mission. These can be passed in parameters or could come also from other contexts.
 */
public interface In {

    <T> T get(Placeholder<T> placeholder);

}
