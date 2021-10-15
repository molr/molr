package io.molr.mole.core.support;

import static io.molr.mole.core.support.MissionPredicates.runStateEquals;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.single.SingleNodeMission;
import io.molr.mole.core.single.SingleNodeMole;

public class OngoingMissionRunTest {

    private OngoingMissionRun run;

    @Before
    public void setUp() {
        SingleNodeMission<Void> voidMission0 = SingleNodeMission.from(() -> {
            // void return type
        });
        Mole mole = new SingleNodeMole(new HashSet<>(Collections.singletonList(voidMission0)));
        MissionControlSupport support = MissionControlSupport.from(mole);
        run = support.start(voidMission0.name(), new HashMap<>());
    }

    @Test
    public void and() {
        assertNotNull(run.and());
        assertThat(run.and(), instanceOf(OngoingMissionRun.class));
    }

    @Test
    public void forget() {
        run.forget();
    }

    @Test
    public void mole() {
        assertNotNull(run.mole());
        assertThat(run.mole(), instanceOf(Mole.class));
    }

    @Test
    public void awaitFinished() {
        assertNotNull(run.awaitFinished());
        assertThat(run.awaitFinished().block(), instanceOf(Result.class));
        assertEquals(Result.SUCCESS, run.awaitFinished().block());

        assertNotNull(run.awaitFinished(Duration.ofMillis(100)));
        assertThat(run.awaitFinished(Duration.ofMillis(100)).block(), instanceOf(Result.class));
        assertEquals(Result.SUCCESS, run.awaitFinished(Duration.ofMillis(100)).block());
    }

    @Test
    public void asyncHandle() {
        assertNotNull(run.asyncHandle());
    }

    @Test
    public void awaitHandle() {
        assertNotNull(run.awaitHandle());
        assertThat(run.awaitHandle(), instanceOf(MissionHandle.class));
    }

    @Test
    public void await() {
        run.await(runStateEquals(RunState.FINISHED));
        run.await(runStateEquals(RunState.FINISHED), Duration.ofMillis(100));
    }

    @Test
    public void returnResult() {
        OngoingMissionRun.ReturnHelper<Result> resultReturnHelper = run.returnResult();

        assertNotNull(resultReturnHelper.whenFinished());
        assertThat(resultReturnHelper.whenFinished(), instanceOf(Result.class));
        assertEquals(Result.SUCCESS, resultReturnHelper.whenFinished());

        assertNotNull(resultReturnHelper.whenFinished(Duration.ofMillis(100)));
        assertThat(resultReturnHelper.whenFinished(Duration.ofMillis(100)), instanceOf(Result.class));
        assertEquals(Result.SUCCESS, resultReturnHelper.whenFinished(Duration.ofMillis(100)));

        assertNotNull(resultReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED))));
        assertThat(resultReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED))),
                instanceOf(Result.class));
        assertEquals(Result.SUCCESS, resultReturnHelper.when(runStateEquals(RunState.PAUSED)
                .or(runStateEquals(RunState.FINISHED))));

        assertNotNull(resultReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED)),
                Duration.ofMillis(100)));
        assertThat(resultReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED)),
                Duration.ofMillis(100)), instanceOf(Result.class));
        assertEquals(Result.SUCCESS, resultReturnHelper.when(runStateEquals(RunState.PAUSED)
                .or(runStateEquals(RunState.FINISHED)), Duration.ofMillis(100)));
    }

    @Test
    public void returnState() {
        OngoingMissionRun.ReturnHelper<MissionState> stateReturnHelper = run.returnState();

        assertNotNull(stateReturnHelper.whenFinished());
        assertThat(stateReturnHelper.whenFinished(), instanceOf(MissionState.class));

        assertNotNull(stateReturnHelper.whenFinished(Duration.ofMillis(100)));
        assertThat(stateReturnHelper.whenFinished(Duration.ofMillis(100)), instanceOf(MissionState.class));

        assertNotNull(stateReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED))));
        assertThat(stateReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED))),
                instanceOf(MissionState.class));

        assertNotNull(stateReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED)),
                Duration.ofMillis(100)));
        assertThat(stateReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED)),
                Duration.ofMillis(100)), instanceOf(MissionState.class));
    }
}