package org.molr.mole.api;

import org.molr.commons.api.domain.Mission;

import java.util.Map;

public interface MissionExecutorFactory {

    MissionExecutor executorFor(Mission mission, Map<String, Object> params);


}
