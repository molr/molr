package cern.molr.client.serviceimpl.custom.adapter;

import cern.molr.exception.custom.UnsupportedOutputTypeException;
import cern.molr.mission.service.custom.CustomMissionExecutionService;
import cern.molr.mission.service.custom.adapter.FutureRunMissionController;
import cern.molr.mission.service.custom.adapter.MissionExecutionServiceAdapter;

/**
 * It adapts {@link CustomMissionExecutionService}, used to throw meaningful exceptions
 *
 * @author yassine-kr
 */
public class MissionExecutionServiceAdapterImpl implements MissionExecutionServiceAdapter {

    private CustomMissionExecutionService mExecService;

    public MissionExecutionServiceAdapterImpl(CustomMissionExecutionService mExecService) {
        this.mExecService = mExecService;
    }

    @Override
    public <I, O> FutureRunMissionController<O> runToCompletion(String missionDefnClassName, I args, Class<I> cI, Class<O> cO) throws UnsupportedOutputTypeException {
        return new FutureRunMissionControllerImpl<>(mExecService.runToCompletion(missionDefnClassName,args,cI,cO));
    }
}
