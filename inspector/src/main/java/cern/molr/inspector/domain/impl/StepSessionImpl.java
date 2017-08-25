package cern.molr.inspector.domain.impl;

import java.time.ZonedDateTime;

import cern.molr.inspector.controller.StatefulJdiController;
import cern.molr.inspector.domain.StepSession;
import cern.molr.mission.Mission;

/**
 * @see StepSession
 */
public class StepSessionImpl implements StepSession {

    private final ZonedDateTime timestamp = ZonedDateTime.now();
    private final Mission mission;
    private final StatefulJdiController controller;

    public StepSessionImpl(Mission mission, StatefulJdiController controller) {
        this.mission = mission;
        this.controller = controller;
    }

    @Override
    public Mission getMission() {
        return mission;
    }

    @Override
    public StatefulJdiController getController() {
        return controller;
    }

    @Override
    public ZonedDateTime getTimeStamp() {
        return timestamp;
    }

}
