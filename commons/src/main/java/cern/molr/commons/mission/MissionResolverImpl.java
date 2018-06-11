package cern.molr.commons.mission;

import cern.molr.commons.exception.MissionResolvingException;

/**
 * A simple implementation which uses the current class loader
 *
 * @author yassine-kr
 */
public class MissionResolverImpl implements MissionResolver{
    @Override
    public Class<?> resolve(String missionName) throws MissionResolvingException {
        try {
            return Class.forName(missionName);
        } catch (ClassNotFoundException error) {
            throw new MissionResolvingException(error);
        }
    }
}
