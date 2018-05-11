package cern.molr.mission.service.custom.adapter;

import cern.molr.exception.custom.UnsupportedOutputTypeException;
import cern.molr.mission.service.custom.CustomMissionExecutionService;

/**
 * It adapts {@link CustomMissionExecutionService}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public interface MissionExecutionServiceAdapter {
    <I,O> FutureRunMissionController<O> runToCompletion(String missionDefnClassName, I args, Class<I> cI, Class<O> cO) throws UnsupportedOutputTypeException;
}
