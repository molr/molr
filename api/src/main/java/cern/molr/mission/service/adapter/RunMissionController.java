package cern.molr.mission.service.adapter;

/**
 * It adapts {@link cern.molr.mission.run.RunMissionController}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public interface RunMissionController<O> {
    FutureRunMissionResult<O> getResult();
    FutureRunMissionCancelResult cancel();
}
