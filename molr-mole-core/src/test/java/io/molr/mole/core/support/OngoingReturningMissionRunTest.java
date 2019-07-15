package io.molr.mole.core.support;

import io.molr.commons.domain.RunState;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.single.SingleNodeMission;
import io.molr.mole.core.single.SingleNodeMole;
import io.molr.mole.core.support.domain.MissionStub0;
import org.junit.Before;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

import static io.molr.mole.core.support.MissionPredicates.runStateEqualsTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class OngoingReturningMissionRunTest {

    Mole mole;
    MissionControlSupport support;
    String booleanMission0;
    private OngoingReturningMissionRun<Boolean> run;

    @Before
    public void setUp() {
        SingleNodeMission<Boolean> booleanMission0 = SingleNodeMission.from(Boolean.class, () -> {
            return Boolean.TRUE;
        });
        this.booleanMission0 = booleanMission0.name();
        mole = new SingleNodeMole(new HashSet<>(Arrays.asList(booleanMission0)));
        support = MissionControlSupport.from(mole);
        MissionStub0<Boolean> missionStub0 = MissionStubs.stub(this.booleanMission0).returning(Boolean.class);
        run = support.start(missionStub0);
    }

    @Test
    public void and() {
        assertNotNull(run.and());
        assertThat(run.and(), instanceOf(OngoingReturningMissionRun.class));
    }

    @Test
    public void returnOutput() {
        OngoingMissionRun.ReturnHelper<Boolean> resultReturnHelper = run.returnOutput();

        assertNotNull(resultReturnHelper.whenFinished());
        assertThat(resultReturnHelper.whenFinished(), instanceOf(Boolean.class));
        assertEquals(resultReturnHelper.whenFinished(), Boolean.TRUE);

        assertNotNull(resultReturnHelper.whenFinished(Duration.ofMillis(100)));
        assertThat(resultReturnHelper.whenFinished(Duration.ofMillis(100)), instanceOf(Boolean.class));
        assertEquals(resultReturnHelper.whenFinished(Duration.ofMillis(100)), Boolean.TRUE);

        assertNotNull(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED)));
        assertThat(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED)),
                instanceOf(Boolean.class));
        assertEquals(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED)), Boolean.TRUE);

        assertNotNull(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED),
                Duration.ofMillis(100)));
        assertThat(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED),
                Duration.ofMillis(100)), instanceOf(Boolean.class));
        assertEquals(resultReturnHelper.when(runStateEqualsTo(RunState.PAUSED).or(RunState.FINISHED),
                Duration.ofMillis(100)), Boolean.TRUE);
    }

    @Test
    public void awaitOuputValue() {
        assertNotNull(run.awaitOuputValue());
        assertThat(run.awaitOuputValue(), instanceOf(Boolean.class));
        assertEquals(run.awaitOuputValue(), Boolean.TRUE);
    }
}