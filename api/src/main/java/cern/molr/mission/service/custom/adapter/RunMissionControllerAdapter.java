package cern.molr.mission.service.custom.adapter;

import cern.molr.mission.controller.custom.CustomRunMissionController;

/**
 * It adapts {@link CustomRunMissionController}, used to throw meaningful exceptions
 *
 * @author yassine-kr
 */
public interface RunMissionControllerAdapter<O> {
    FutureRunMissionResult<O> getResult();
    FutureRunMissionCancelResult cancel();
}
