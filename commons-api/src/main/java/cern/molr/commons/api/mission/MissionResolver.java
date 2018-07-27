package cern.molr.commons.api.mission;

import cern.molr.commons.api.exception.MissionResolvingException;

/**
 * A resolver which resolves the mission {@link Class} corresponding to a mission name.
 *
 * @author yassine-kr
 */
public interface MissionResolver {

    Class<?> resolve(String missionName) throws MissionResolvingException;
}
