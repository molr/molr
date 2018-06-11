package cern.molr.commons.mission;

import cern.molr.commons.exception.MissionResolvingException;

/**
 * A resolver which resolves the mission class corresponding to a mission name.
 * @author yassine-kr
 */
public interface MissionResolver {

    Class<?> resolve(String missionName) throws MissionResolvingException;

    MissionResolver defaultMissionResolver = new MissionResolverImpl();
}
