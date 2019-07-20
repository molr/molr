package io.molr.mole.core.support;

import io.molr.commons.domain.RunState;
import org.junit.Test;

import java.util.function.Predicate;

import static io.molr.mole.core.support.MissionPredicates.runStateEquals;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class MissionPredicatesTest {

    @Test
    public void runState() {
        Predicate<RunState> runStateValidator = runStateEquals(RunState.FINISHED);
        assertNotNull(runStateValidator);
        assertThat(runStateValidator, instanceOf(Predicate.class));
    }

    @Test
    public void or() {
        Predicate<RunState> runStateValidator = runStateEquals(RunState.FINISHED).or(runStateEquals(RunState.RUNNING));
        assertTrue(runStateValidator.test(RunState.RUNNING));
        assertTrue(runStateValidator.test(RunState.FINISHED));
        assertFalse(runStateValidator.test(RunState.PAUSED));
    }
}