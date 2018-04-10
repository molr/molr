package cern.molr.client.serviceimpl.adapter;

import cern.molr.exception.UnsupportedOutputTypeException;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.mission.service.adapter.FutureRunMissionController;
import cern.molr.mission.service.adapter.MissionExecutionServiceAdapter;

/**
 * It adapts {@link MissionExecutionService}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public class MissionExecutionServiceAdapterImpl implements MissionExecutionServiceAdapter {

    private MissionExecutionService mExecService;

    public MissionExecutionServiceAdapterImpl(MissionExecutionService mExecService) {
        this.mExecService = mExecService;
    }

    @Override
    public <I, O> FutureRunMissionController<O> runToCompletion(String missionDefnClassName, I args, Class<I> cI, Class<O> cO) throws UnsupportedOutputTypeException {
        return new FutureRunMissionControllerImpl<>(mExecService.runToCompletion(missionDefnClassName,args,cI,cO));
    }
}
