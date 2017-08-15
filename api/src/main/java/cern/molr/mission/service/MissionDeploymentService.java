/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.mission.service;

import cern.molr.mission.Mission;

/**
 * {@link MissionDeploymentService} is the client (developer) - server interface used to deploy (or submit) missions+
 * 
 * @author nachivpn
 */
public interface MissionDeploymentService {

    public void deploy(Mission m);
    
}
