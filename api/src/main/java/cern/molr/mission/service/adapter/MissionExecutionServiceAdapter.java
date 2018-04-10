package cern.molr.mission.service.adapter;

import cern.molr.exception.UnsupportedOutputTypeException;
import cern.molr.mission.service.MissionExecutionService;

/**
 * It adapts {@link MissionExecutionService}, used to throw meaningful exceptions
 *
 * @author yassine
 */
public interface MissionExecutionServiceAdapter {
    <I,O> FutureRunMissionController<O> runToCompletion(String missionDefnClassName, I args, Class<I> cI, Class<O> cO) throws UnsupportedOutputTypeException;
}
