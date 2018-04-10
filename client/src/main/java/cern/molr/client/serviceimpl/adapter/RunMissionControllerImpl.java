package cern.molr.client.serviceimpl.adapter;

import cern.molr.mission.service.adapter.FutureRunMissionCancelResult;
import cern.molr.mission.service.adapter.FutureRunMissionResult;
import cern.molr.mission.service.adapter.RunMissionController;

/**
 * It adapts {@link cern.molr.mission.run.RunMissionController}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public class RunMissionControllerImpl<O> implements RunMissionController<O> {

    private cern.molr.mission.run.RunMissionController<O> controller;

    public RunMissionControllerImpl(cern.molr.mission.run.RunMissionController<O> controller) {
        this.controller = controller;
    }

    @Override
    public FutureRunMissionResult<O> getResult() {
        return new FutureRunMissionResultImpl<O>(controller.getResult());
    }

    @Override
    public FutureRunMissionCancelResult cancel() {
        return new FutureRunMissionCancelResultImpl(controller.cancel());
    }
}
