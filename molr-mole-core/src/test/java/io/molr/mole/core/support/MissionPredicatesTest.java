package io.molr.mole.core.support;

import io.molr.commons.domain.RunState;
import org.junit.Test;

import static io.molr.mole.core.support.MissionPredicates.runStateEqualsTo;
import static org.junit.Assert.*;

public class MissionPredicatesTest {

    @Test
    public void runState() {
        MissionPredicates<RunState> runStateValidator = runStateEqualsTo(RunState.FINISHED);
        assertNotNull(runStateValidator);
        assertEquals(MissionPredicates.class, runStateValidator.getClass());
    }

    @Test
    public void or() {
        MissionPredicates<RunState> runStateValidator = runStateEqualsTo(RunState.FINISHED).or(RunState.RUNNING);
        assertTrue(runStateValidator.test(RunState.RUNNING));
        assertTrue(runStateValidator.test(RunState.FINISHED));
        assertFalse(runStateValidator.test(RunState.PAUSED));
    }

    @Test
    public void and() {
        MissionPredicates<RunState> runStateValidator1 = runStateEqualsTo(RunState.FINISHED).and(RunState.RUNNING);
        assertFalse(runStateValidator1.test(RunState.RUNNING));
        assertFalse(runStateValidator1.test(RunState.FINISHED));
        assertFalse(runStateValidator1.test(RunState.PAUSED));

        MissionPredicates<RunState> runStateValidator2 = runStateEqualsTo(RunState.FINISHED).and(RunState.FINISHED);
        assertFalse(runStateValidator2.test(RunState.RUNNING));
        assertTrue(runStateValidator2.test(RunState.FINISHED));
    }
}