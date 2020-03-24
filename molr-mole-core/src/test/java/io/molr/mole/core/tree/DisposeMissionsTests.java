package io.molr.mole.core.tree;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

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
import io.molr.mole.core.runnable.lang.Branch;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;

public class DisposeMissionsTests {

    private final static Logger LOGGER = LoggerFactory.getLogger(DisposeMissionsTests.class);
    
    Mole mole;
    Mission demoMission = new Mission(demoMission().name());

    @Before
    public void prepareMoleAndMissions() {
        Set<RunnableLeafsMission> missions = Sets.newHashSet();
        missions.add(demoMission());
        mole = new RunnableLeafsMole(missions);
    }

    @Test
    public void ckeckThatDisposeCommandIsAllowedIffMissionIsCompleted() {
        MissionHandle instance1Handle = mole.instantiate(demoMission, Maps.newHashMap()).block();
        MissionHandle instance2Handle = mole.instantiate(demoMission, Maps.newHashMap()).block();

        sleepUnchecked(1000);

        MissionState instance1StateAfterInstantiating = mole.statesFor(instance1Handle).blockFirst();
        assertFalse(instance1StateAfterInstantiating.allowedMissionCommands().contains(MissionCommand.DISPOSE));

        MissionState instance2StateAfterInstantiating = mole.statesFor(instance2Handle).blockFirst();
        assertFalse(instance2StateAfterInstantiating.allowedMissionCommands().contains(MissionCommand.DISPOSE));

        mole.instructRoot(instance1Handle, StrandCommand.RESUME);
        sleepUnchecked(1000);

        MissionState instance1StateAfterMissionCompleted = mole.statesFor(instance1Handle).blockFirst();
        assertTrue(instance1StateAfterMissionCompleted.allowedMissionCommands().contains(MissionCommand.DISPOSE));
    }

    @Test
    public void assertThatDisposedMissionInstancesAreNoLongerAccessible() {
        MissionHandle instance1Handle = mole.instantiate(demoMission, Maps.newHashMap()).block();
        MissionHandle instance2Handle = mole.instantiate(demoMission, Maps.newHashMap()).block();

        sleepUnchecked(1000);
        MissionInstance instance1 = new MissionInstance(instance1Handle, demoMission);
        MissionInstance instance2 = new MissionInstance(instance2Handle, demoMission);

        AgencyState agencyState = mole.states().blockFirst();
        assertTrue(agencyState.activeMissions().contains(instance1));
        assertTrue(agencyState.activeMissions().contains(instance2));

        mole.instructRoot(instance1Handle, StrandCommand.RESUME);
        mole.instructRoot(instance2Handle, StrandCommand.RESUME);
        sleepUnchecked(1000);
        
        mole.instruct(instance2Handle, MissionCommand.DISPOSE);
        sleepUnchecked(1000);
        agencyState = mole.states().blockFirst();
        assertTrue(agencyState.activeMissions().contains(instance1));
        assertFalse(agencyState.activeMissions().contains(instance2));
        
        mole.instruct(instance1Handle, MissionCommand.DISPOSE);
        sleepUnchecked(1000);
        agencyState = mole.states().blockFirst();
        assertFalse(agencyState.activeMissions().contains(instance1));
        assertFalse(agencyState.activeMissions().contains(instance2));
    }

    private static void sleepUnchecked(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * 
     */
    private static RunnableLeafsMission demoMission() {
        return new RunnableLeafsMissionSupport() {
            {
                sequential("Executable Leafs Demo Mission", root -> {

                    root.sequential("First", b -> {
                        b.run(log("First A"));
                        b.run(log("First B"));
                    });

                    root.sequential("Second", b -> {
                        b.run(log("second A"));
                        b.run(log("second B"));
                    });

                    root.run(log("Third"));

                    root.parallel("Parallel", b -> {
                        b.run(log("Parallel A"));
                        b.run(log("parallel B"));
                    });

                });

            }
        }.build();
    }

    private static Branch.Task log(String text) {
        return new Branch.Task(text, (in, out) -> LOGGER.info(text));
    }

}
