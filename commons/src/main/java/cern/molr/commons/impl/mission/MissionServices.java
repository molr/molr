package cern.molr.commons.impl.mission;

import cern.molr.commons.api.mission.MissionMaterializer;
import cern.molr.commons.api.mission.MissionResolver;

/**
 * Class wrapping the services used for managing missions
 * @author yassine-kr
 */
public class MissionServices {

    /**
     * The resolver used to deduce the class corresponding to a mission name
     */
    private static final MissionResolver resolver = new ClassNameResolver();

    /**
     * The materializer used to deduce the mole class name corresponding to a mission name
     */
    private static final MissionMaterializer materializer = new AnnotatedMissionMaterializer();

    public static MissionResolver getResolver() {
        return resolver;
    }

    public static MissionMaterializer getMaterializer() {
        return materializer;
    }
}

