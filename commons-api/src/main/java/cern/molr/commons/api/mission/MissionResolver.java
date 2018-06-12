package cern.molr.commons.api.mission;

import cern.molr.commons.api.exception.MissionResolvingException;

/**
 * A resolver which resolves the mission class corresponding to a mission name.
 *
 * @author yassine-kr
 */
public interface MissionResolver {

    /**
     * A simple implementation which uses the current class loader
     */
    MissionResolver defaultMissionResolver = new MissionResolver() {
        @Override
        public Class<?> resolve(String missionName) throws MissionResolvingException {
            try {
                return Class.forName(missionName);
            } catch (ClassNotFoundException error) {
                throw new MissionResolvingException(error);
            }
        }
    };

    Class<?> resolve(String missionName) throws MissionResolvingException;
}
