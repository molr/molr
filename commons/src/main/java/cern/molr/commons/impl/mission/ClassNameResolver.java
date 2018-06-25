package cern.molr.commons.impl.mission;

import cern.molr.commons.api.exception.MissionResolvingException;
import cern.molr.commons.api.mission.MissionResolver;

/**
 * A simple implementation which uses the current class loader
 * @author yassine-kr
 */
public class ClassNameResolver implements MissionResolver {

    @Override
    public Class<?> resolve(String missionName) throws MissionResolvingException {
        try {
            return Class.forName(missionName);
        } catch (ClassNotFoundException error) {
            throw new MissionResolvingException(error);
        }
    }
}
