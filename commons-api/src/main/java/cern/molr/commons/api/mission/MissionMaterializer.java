/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.mission;

import cern.molr.commons.api.exception.MissionMaterializationException;

/**
 * Used to converts a given mission name into a mission object. Actually it deduces the mole class name corresponding
 * to the mission
 *
 * @author nachivpn
 * @author yassine-kr
 */
public interface MissionMaterializer {

    /**
     * Converts the mission name to a mission object
     *
     * @return the mission object containing the mole {@link Class} name
     */
    Mission materialize(String missionName) throws MissionMaterializationException;

}
