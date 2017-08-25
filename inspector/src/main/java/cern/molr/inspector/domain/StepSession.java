/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.inspector.domain;

import java.time.ZonedDateTime;

import cern.molr.inspector.controller.StatefulJdiController;
import cern.molr.mission.Mission;
import cern.molr.mission.step.StepResult;

/**
 * A generic {@link StepSession}, encapsulates the information of a currently stepping {@link Mission}
 * NOTE: This is the inspector supported Step version, while the new Molr API expects {@link cern.molr.mission.step.StepSession}
 * Either the API has to be changed to support this version of inspector, or vice-versa
 * 
 * @author tiagomr
 * @author nachivpn
 */
public interface StepSession{

    /**
     * @return the {@link Mission} being executed
     */
    Mission getMission();
    
    StatefulJdiController getController();

    /**
     * @return a {@link ZonedDateTime} to timestamp the creation of the {@link StepSession}
     */
    ZonedDateTime getTimeStamp();
    
}
