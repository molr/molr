package cern.molr.client.serviceimpl.custom.adapter;

import cern.molr.mission.controller.custom.CustomRunMissionController;
import cern.molr.mission.service.custom.adapter.FutureRunMissionCancelResult;
import cern.molr.mission.service.custom.adapter.FutureRunMissionResult;
import cern.molr.mission.service.custom.adapter.RunMissionControllerAdapter;

/**
 * It adapts {@link CustomRunMissionController}, used to throw meaningful exceptions
 *
 * @author yassine-kr
 */
public class RunMissionControllerAdapterImpl<O> implements RunMissionControllerAdapter<O> {

    private CustomRunMissionController<O> controller;

    public RunMissionControllerAdapterImpl(CustomRunMissionController<O> controller) {
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
