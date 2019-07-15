package io.molr.mole.core.support;

import io.molr.commons.domain.RunState;
import org.junit.Test;

import static org.junit.Assert.*;

public class MissionPredicatesTest {

    @Test
    public void runState() {
        MissionPredicates<RunState> runStateValidator = MissionPredicates.runStatePredicate(RunState.FINISHED);
        assertNotNull(runStateValidator);
        assertEquals(MissionPredicates.class, runStateValidator.getClass());
    }

    @Test
    public void or() {
        MissionPredicates<RunState> runStateValidator = MissionPredicates.runStatePredicate(RunState.FINISHED).or(RunState.RUNNING);
        assertTrue(runStateValidator.test(RunState.RUNNING));
        assertTrue(runStateValidator.test(RunState.FINISHED));
        assertFalse(runStateValidator.test(RunState.PAUSED));
    }

    @Test
    public void and() {
        MissionPredicates<RunState> runStateValidator1 = MissionPredicates.runStatePredicate(RunState.FINISHED).and(RunState.RUNNING);
        assertFalse(runStateValidator1.test(RunState.RUNNING));
        assertFalse(runStateValidator1.test(RunState.FINISHED));
        assertFalse(runStateValidator1.test(RunState.PAUSED));

        MissionPredicates<RunState> runStateValidator2 = MissionPredicates.runStatePredicate(RunState.FINISHED).and(RunState.FINISHED);
        assertFalse(runStateValidator2.test(RunState.RUNNING));
        assertTrue(runStateValidator2.test(RunState.FINISHED));
    }
}