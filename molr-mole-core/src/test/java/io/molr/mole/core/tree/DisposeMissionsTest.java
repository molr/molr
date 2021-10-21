package io.molr.mole.core.tree;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.molr.commons.domain.AgencyState;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionCommand;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionInstance;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.api.Mole;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.RunnableLeafsMole;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;
import io.molr.mole.core.runnable.lang.SimpleBranch;

public class DisposeMissionsTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(DisposeMissionsTest.class);

    Mole mole;
    Mission demoMission = new Mission(demoMission().name());

    @Before
    public void prepareMoleAndMissions() {
        mole = new RunnableLeafsMole(Sets.newHashSet(demoMission()));
    }

    @Test
    public void ckeckThatDisposeCommandIsAllowedIffMissionIsCompleted() {
        MissionHandle instance1Handle = mole.instantiate(demoMission, Maps.newHashMap()).block();
        MissionHandle instance2Handle = mole.instantiate(demoMission, Maps.newHashMap()).block();

        sleepUnchecked(1000);

        MissionState instance1StateAfterInstantiating = mole.statesFor(instance1Handle).blockFirst();
        assertThat(instance1StateAfterInstantiating.allowedMissionCommands()).isEmpty();

        MissionState instance2StateAfterInstantiating = mole.statesFor(instance2Handle).blockFirst();
        assertThat(instance2StateAfterInstantiating.allowedMissionCommands()).isEmpty();

        mole.instructRoot(instance1Handle, StrandCommand.RESUME);

        MissionState instance1StateAfterMissionCompleted = mole.statesFor(instance1Handle).blockLast();
        assertThat(instance1StateAfterMissionCompleted.allowedMissionCommands()).containsExactly(MissionCommand.DISPOSE);
    }

    @Test
    public void assertThatDisposedMissionInstancesAreNoLongerAccessible() {
        MissionHandle instance1Handle = mole.instantiate(demoMission, Maps.newHashMap()).block();
        MissionHandle instance2Handle = mole.instantiate(demoMission, Maps.newHashMap()).block();

        sleepUnchecked(1000);
        MissionInstance instance1 = new MissionInstance(instance1Handle, demoMission);
        MissionInstance instance2 = new MissionInstance(instance2Handle, demoMission);

        AgencyState agencyState = mole.states().blockFirst();
        assertThat(agencyState.activeMissions()).containsExactlyInAnyOrder(instance1, instance2);

        mole.instructRoot(instance1Handle, StrandCommand.RESUME);
        mole.instructRoot(instance2Handle, StrandCommand.RESUME);
        sleepUnchecked(1000);

        mole.instruct(instance2Handle, MissionCommand.DISPOSE);
        sleepUnchecked(1000);
        agencyState = mole.states().blockFirst();
        assertThat(agencyState.activeMissions()).containsExactly(instance1);

        mole.instruct(instance1Handle, MissionCommand.DISPOSE);
        sleepUnchecked(1000);
        agencyState = mole.states().blockFirst();
        assertThat(agencyState.activeMissions()).isEmpty();
    }

    private static void sleepUnchecked(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static RunnableLeafsMission demoMission() {
        return new RunnableLeafsMissionSupport() {
            {
                root("Executable Leafs Demo Mission").sequential().as(root -> {

                    root.branch("First").sequential().as(b1 -> {
                        log(b1, "First A");
                        log(b1, "First B");
                    });

                    root.branch("Second").sequential().as(b1 -> {
                        log(b1, "second A");
                        log(b1, "second B");
                    });

                    log(root, "Third");

                    root.branch("Parallel").parallel().as(b -> {
                        log(b, "Parallel A");
                        log(b, "parallel B");
                    });

                });

            }
        }.build();
    }

    private static void log(SimpleBranch b, String text) {
        b.leaf(text).run(() -> LOGGER.info(text));
    }

}
