package org.molr.mole.core.api;

import org.molr.commons.domain.Mission;

import java.util.Map;

public interface MissionExecutorFactory {

    MissionExecutor executorFor(Mission mission, Map<String, Object> params);


}
