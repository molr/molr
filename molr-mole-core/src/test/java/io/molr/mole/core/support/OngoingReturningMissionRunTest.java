package io.molr.mole.core.support;

import static io.molr.commons.domain.Placeholder.aDouble;
import static io.molr.commons.domain.Placeholder.aString;
import static io.molr.mole.core.support.MissionPredicates.runStateEquals;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import io.molr.commons.domain.RunState;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.single.SingleNodeMission;
import io.molr.mole.core.single.SingleNodeMole;
import io.molr.mole.core.support.domain.MissionStub0;
import io.molr.mole.core.support.domain.MissionStub2;
import io.molr.mole.core.support.domain.VoidStub0;

public class OngoingReturningMissionRunTest {

    private MissionControlSupport support;
    private String voidMission0;
    private String booleanMission2;
    private OngoingReturningMissionRun<Boolean> run;

    @Before
    public void setUp() {
        SingleNodeMission<Boolean> booleanMission0 = SingleNodeMission.from(Boolean.class, () -> TRUE);
        SingleNodeMission<Void> voidMission0 = SingleNodeMission.from(() -> {
            // void return type
        });
        this.voidMission0 = voidMission0.name();
        SingleNodeMission<Boolean> booleanMission2 = SingleNodeMission.from(Boolean.class, (p1, p2) -> TRUE,
                aDouble("doubleParam"), aString("stringParam"));
        this.booleanMission2 = booleanMission2.name();
        Mole mole = new SingleNodeMole(new HashSet<>(Arrays.asList(booleanMission0, voidMission0, booleanMission2)));
        support = MissionControlSupport.from(mole);
        MissionStub0<Boolean> missionStub0 = MissionStubs.stub(booleanMission0.name()).returning(Boolean.class);
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
        assertEquals(TRUE, resultReturnHelper.whenFinished());

        assertNotNull(resultReturnHelper.whenFinished(Duration.ofMillis(100)));
        assertThat(resultReturnHelper.whenFinished(Duration.ofMillis(100)), instanceOf(Boolean.class));
        assertEquals(TRUE, resultReturnHelper.whenFinished(Duration.ofMillis(100)));

        assertNotNull(resultReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED))));
        assertThat(resultReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED))),
                instanceOf(Boolean.class));
        assertEquals(TRUE, resultReturnHelper.when(runStateEquals(RunState.PAUSED)
                .or(runStateEquals(RunState.FINISHED))));

        assertNotNull(resultReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED)),
                Duration.ofMillis(100)));
        assertThat(resultReturnHelper.when(runStateEquals(RunState.PAUSED).or(runStateEquals(RunState.FINISHED)),
                Duration.ofMillis(100)), instanceOf(Boolean.class));
        assertEquals(TRUE, resultReturnHelper.when(runStateEquals(RunState.PAUSED)
                .or(runStateEquals(RunState.FINISHED)), Duration.ofMillis(100)));
    }

    @Test
    public void awaitOuputValue() {
        assertNotNull(run.awaitOuputValue());
        assertThat(run.awaitOuputValue(), instanceOf(Boolean.class));
        assertEquals(TRUE, run.awaitOuputValue());
    }

    @Test
    public void startAndAwaitOutputValue() {
        MissionStub2<Double, String, Boolean> missionStub2 = MissionStubs.stub(booleanMission2).returning(Boolean.class)
                .withParameters(aDouble("doubleParam"), aString("stringParam"));
        Boolean success = support.start(missionStub2, 0.1, "We did it!")
                .and().awaitOuputValue();
        assertTrue(success);
    }

    @Test
    public void startAndForget() {
        VoidStub0 voidStub0 = MissionStubs.stub(voidMission0);
        support.start(voidStub0).and().forget();
    }
}