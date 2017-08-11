/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.sample.client;

import cern.molr.exception.MissionMaterializationException;
import cern.molr.mission.Mission;
import cern.molr.mission.MissionMaterializer;
import cern.molr.mission.service.MissionDeploymentService;
import cern.molr.sample.mission.IntDoubler;
import cern.molr.sample.mission.RunnableHelloWriter;

/**
 * Sample implementation and usage of the developer's interfaces to demonstrate communication
 * 
 * @author nachivpn
 */
public class SampleDeveloper {

    MissionMaterializer materializer;
    MissionDeploymentService mDService;
    
    public SampleDeveloper(MissionMaterializer materializer, MissionDeploymentService mDService) {
        this.materializer = materializer;
        this.mDService = mDService;
    }
    
    public void developerDeploy() throws MissionMaterializationException {
        Mission mission1 = materializer.materialize(RunnableHelloWriter.class);
        mDService.deploy(mission1);
        Mission mission2 = materializer.materialize(IntDoubler.class);
        mDService.deploy(mission2);
    }

}
