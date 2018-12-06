package org.molr.mole.core.api;

import static java.lang.String.format;

/**
 * Contains all the REST API endpoints for a mole
 */
public final class MoleWebApi {

    public static final String MISSION_REPRESENTATION_MISSION_NAME = "missionName";
    public static final String MISSION_REPRESENTATION_PATH = "/mission/{missionName}/representation";

    public static String missionRepresentationUrl(String missionName) {
        return format("/mission/%s/representation", missionName);
    }

    private MoleWebApi() {
        /* only static methods */
    }
}
