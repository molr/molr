/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client.api;


/**
 * {@link MissionDeploymentService} is the client (developer) - server interface used to deploy (or submit) missions
 *
 * @author nachivpn
 * @author yassine-kr
 */
public interface MissionDeploymentService {

    void deploy(String missionName);

}
