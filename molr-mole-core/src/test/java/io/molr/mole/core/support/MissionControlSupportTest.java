package io.molr.mole.core.support;

import static io.molr.commons.domain.Placeholder.aDouble;
import static io.molr.commons.domain.Placeholder.aString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.assertj.core.util.Maps;
import org.junit.Before;
import org.junit.Test;

import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionHandle;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.single.SingleNodeMission;
import io.molr.mole.core.single.SingleNodeMole;
import io.molr.mole.core.support.domain.MissionStub0;
import io.molr.mole.core.support.domain.MissionStub1;
import io.molr.mole.core.support.domain.MissionStub2;
import io.molr.mole.core.support.domain.VoidStub0;
import io.molr.mole.core.support.domain.VoidStub1;
import io.molr.mole.core.support.domain.VoidStub2;
import reactor.core.publisher.Mono;

public class MissionControlSupportTest {

    private Mole mole;
    private MissionControlSupport support;
    private String voidMission0;
    private String voidMission1;
    private String voidMission2;
    private String booleanMission0;
    private String integerMission1;
    private String doubleMission2;

    @Before
    public void setUp() {
        SingleNodeMission<Void> voidMission0 = SingleNodeMission.from(() -> {
            // void return type
        });
        this.voidMission0 = voidMission0.name();
        SingleNodeMission<Void> voidMission1 = SingleNodeMission.from((p1) -> {
            p1.toString();
            // void return type
        }, aString("stringParam"));
        this.voidMission1 = voidMission1.name();
        SingleNodeMission<Void> voidMission2 = SingleNodeMission.from((p1, p2) -> {
            p1.toString();
            p2.doubleValue();
            // void return type
        }, aString("stringParam"), aDouble("doubleParam"));
        this.voidMission2 = voidMission2.name();
        SingleNodeMission<Boolean> booleanMission0 = SingleNodeMission.from(Boolean.class, () -> {
            return Boolean.TRUE;
        });
        this.booleanMission0 = booleanMission0.name();
        SingleNodeMission<Integer> integerMission1 = SingleNodeMission.from(Integer.class, (p1) -> {
            return 1;
        }, aString("stringParam"));
        this.integerMission1 = integerMission1.name();
        SingleNodeMission<Double> doubleMission2 = SingleNodeMission.from(Double.class, (p1, p2) -> {
            return 1.2;
        }, aString("stringParam"), aDouble("doubleParam"));
        this.doubleMission2 = doubleMission2.name();
        mole = new SingleNodeMole(new HashSet<>(Arrays.asList(voidMission0, voidMission1, voidMission2,
                booleanMission0, integerMission1, doubleMission2)));
        support = MissionControlSupport.from(mole);
    }

    @Test
    public void from() {
        assertThat(MissionControlSupport.from(mole), instanceOf(MissionControlSupport.class));
    }

    @Test(expected = NullPointerException.class)
    public void fromThrowsNullPointerExceptionOnNullMole() {
        MissionControlSupport.from(null);
    }

    @Test
    public void start() {
        OngoingMissionRun run;

        assertThat((run = support.start(voidMission1, new HashMap<String, Object>() {{
            put("stringParam", "name");
        }})), instanceOf(OngoingMissionRun.class));
        assertThat(run.awaitHandle(), instanceOf(MissionHandle.class));

        assertThat((run = support.start(new Mission(voidMission2), new HashMap<String, Object>() {{
            put("stringParam", "name");
            put("doubleParam", 1.5);
        }})), instanceOf(OngoingMissionRun.class));
        assertThat(run.awaitHandle(), instanceOf(MissionHandle.class));

        VoidStub0 voidStub0 = MissionStubs.stub(voidMission0);
        assertThat((run = support.start(voidStub0)), instanceOf(OngoingMissionRun.class));
        assertThat(run.awaitHandle(), instanceOf(MissionHandle.class));

        VoidStub1 voidStub1 = MissionStubs.stub(voidMission1).withParameters(aString("stringParam"));
        assertThat((run = support.start(voidStub1, "string parameter")),
                instanceOf(OngoingMissionRun.class));
        assertThat(run.awaitHandle(), instanceOf(MissionHandle.class));

        VoidStub2 voidStub2 = MissionStubs.stub(voidMission2).withParameters(aString("stringParam"),
                aDouble("doubleParam"));
        assertThat((run = support.start(voidStub2, "string parameter", 1.5)),
                instanceOf(OngoingMissionRun.class));
        assertThat(run.awaitHandle(), instanceOf(MissionHandle.class));

        MissionStub0<Boolean> missionStub0 = MissionStubs.stub(booleanMission0).returning(Boolean.class);
        assertThat((run = support.start(missionStub0)), instanceOf(OngoingReturningMissionRun.class));
        assertThat(run.awaitHandle(), instanceOf(MissionHandle.class));

        MissionStub1<String, Integer> missionStub1 = MissionStubs.stub(integerMission1).returning(Integer.class)
                .withParameters(aString("stringParam"));
        assertThat((run = support.start(missionStub1, "string parameter")),
                instanceOf(OngoingReturningMissionRun.class));
        assertThat(run.awaitHandle(), instanceOf(MissionHandle.class));

        MissionStub2<String, Double, Double> missionStub2 = MissionStubs.stub(doubleMission2).returning(Double.class)
                .withParameters(aString("stringParam"), aDouble("doubleParam"));
        assertThat((run = support.start(missionStub2, "string parameter", 0.1)),
                instanceOf(OngoingReturningMissionRun.class));
        assertThat(run.awaitHandle(), instanceOf(MissionHandle.class));
    }

    @Test
    public void control() {
        Mono<MissionHandle> handleMono = support.start(voidMission1, Maps.newHashMap("stringParam","parameter value")).asyncHandle();

        assertThat(support.control(handleMono), instanceOf(OngoingMissionRun.class));
        assertNotNull(support.control(handleMono));

        assertThat(support.control(handleMono.block()), instanceOf(OngoingMissionRun.class));
        assertNotNull(support.control(handleMono.block()));
    }

    @Test(expected = NullPointerException.class)
    public void controlThrowsNullPointerException1() {
        support.control((MissionHandle) null);
    }

    @Test(expected = NullPointerException.class)
    public void controlThrowsNullPointerException2() {
        support.control((Mono<MissionHandle>) null);
    }
}
