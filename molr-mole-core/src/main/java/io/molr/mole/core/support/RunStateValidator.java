package io.molr.mole.core.support;

import io.molr.commons.domain.RunState;

import java.util.function.Predicate;

public class RunStateValidator {
    Predicate<RunState> validator;

    private RunStateValidator(RunState runState) {
        validator = rs -> runState.equals(rs);
    }

    public static RunStateValidator validateState(RunState runState) {
        return new RunStateValidator(runState);
    }

    public RunStateValidator or(RunState runState) {
        validator = validator.or(rs -> runState.equals(rs));
        return this;
    }

    public boolean test(RunState runState) {
        return validator.test(runState);
    }
}
