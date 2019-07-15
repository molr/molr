package io.molr.mole.core.support;

import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.Result;
import io.molr.commons.domain.RunState;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.single.SingleNodeMission;
import io.molr.mole.core.single.SingleNodeMole;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static io.molr.mole.core.support.MissionPredicates.runStateEqualsTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class OngoingMissionRunTest {

    Mole mole;
    MissionControlSupport support;
    String voidMission0;
    private OngoingMissionRun run;

    @Before
    public void setUp() throws Exception {
        SingleNodeMission<Void> voidMission0 = SingleNodeMission.from(() -> {
            // void return type
        });
        this.voidMission0 = voidMission0.name();
        mole = new SingleNodeMole(new HashSet<>(Arrays.asList(voidMission0)));
        support = MissionControlSupport.from(mole);
        run = support.start(this.voidMission0, new HashMap<>());
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
        assertEquals(run.awaitFinished().block(), Result.SUCCESS);

        assertNotNull(run.awaitFinished(Duration.ofMillis(100)));
        assertThat(run.awaitFinished(Duration.ofMillis(100)).block(), instanceOf(Result.class));
        assertEquals(run.awaitFinished(Duration.ofMillis(100)).block(), Result.SUCCESS);
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
        run.await(runStateEqualsTo(RunState.FINISHED));
        run.await(runStateEqualsTo(RunState.FINISHED), Duration.ofMillis(100));
    }

    @Test
    public void returnResult() {
        OngoingMissionRun.ReturnHelper<Result> resultReturnHelper = run.returnResult();

        assertNotNull(resultReturnHelper.whenFinished());
        assertThat(resultReturnHelper.whenFinished(), instanceOf(Result.class));
        assertEquals(resultReturnHelper.whenFinished(), Result.SUCCESS);

        assertNotNull(resultReturnHelper.whenFinished(Duration.ofMillis(100)));
        assertThat(resultReturnHelper.whenFinished(Duration.ofMillis(100)), instanceOf(Result.class));
        assertEquals(resultReturnHelper.whenFinished(Duration.ofMillis(100)), Result.SUCCESS);

        assertNotNull(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED)));
        assertThat(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED)),
                instanceOf(Result.class));
        assertEquals(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED)), Result.SUCCESS);

        assertNotNull(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED),
                Duration.ofMillis(100)));
        assertThat(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED),
                Duration.ofMillis(100)), instanceOf(Result.class));
        assertEquals(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED),
                Duration.ofMillis(100)), Result.SUCCESS);
    }

    @Test
    public void returnState() {
        OngoingMissionRun.ReturnHelper<MissionState> stateReturnHelper = run.returnState();

        assertNotNull(stateReturnHelper.whenFinished());
        assertThat(stateReturnHelper.whenFinished(), instanceOf(MissionState.class));

        assertNotNull(stateReturnHelper.whenFinished(Duration.ofMillis(100)));
        assertThat(stateReturnHelper.whenFinished(Duration.ofMillis(100)), instanceOf(MissionState.class));

        assertNotNull(stateReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED)));
        assertThat(stateReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED)),
                instanceOf(MissionState.class));

        assertNotNull(stateReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED),
                Duration.ofMillis(100)));
        assertThat(stateReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED),
                Duration.ofMillis(100)), instanceOf(MissionState.class));
    }
}