package io.molr.mole.core.support;

import io.molr.commons.domain.RunState;

import java.util.function.Predicate;

/**
 * A general purpose validator for objects.
 *
 * @param <T> the type for object being validated
 * @author HimanshuSahu31
 */
public class MissionPredicates<T> {
    private Predicate<T> validator;

    /**
     * @param object an {@link Object} that represents the initial value
     */
    private MissionPredicates(T object) {
        validator = o -> o.equals(object);
    }

    /**
     * Factory method to get validator for {@link RunState}
     *
     * @param runState the initial value {@link RunState} for predicate function
     * @return a {@link MissionPredicates} for validating {@link RunState}
     */
    public static MissionPredicates<RunState> runStatePredicate(RunState runState) {
        return new MissionPredicates<RunState>(runState);
    }

    /**
     * Adds <strong>or clause</strong> validation to the {@link Predicate} with the argument passed
     *
     * @param object an {@link Object} that represents one of the values of predicate
     * @return a {@link MissionPredicates} with existing validations
     */
    public MissionPredicates or(T object) {
        validator = validator.or(o -> o.equals(object));
        return this;
    }

    /**
     * Adds <strong>and clause</strong> validation to the {@link Predicate} with the argument passed
     *
     * @param object an {@link Object} that represents one of the values of predicate
     * @return a {@link MissionPredicates} with existing validations
     */
    public MissionPredicates and(T object) {
        validator = validator.and(o -> o.equals(object));
        return this;
    }

    /**
     * Validates the argument with {@link Predicate} generated
     *
     * @param object an {@link Object} that needs to be validated
     * @return result of the validation
     */
    public boolean test(T object) {
        return validator.test(object);
    }
}
