package io.molr.mole.core.support;

import io.molr.commons.domain.RunState;

import java.util.function.Predicate;

/**
 * A general purpose validator for objects.
 *
 * @author HimanshuSahu31
 */
public class MissionPredicates {

    /**
     * Factory method to get {@link Predicate} validator for {@link RunState}
     *
     * @param runState the initial value {@link RunState} for predicate function
     * @return a {@link Predicate} for validating {@link RunState}
     */
    public static Predicate<RunState> runStateEquals(RunState runState) {
        return o -> o.equals(runState);
    }
}
