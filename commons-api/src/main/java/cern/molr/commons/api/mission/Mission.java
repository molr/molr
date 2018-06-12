/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.api.mission;

import java.io.Serializable;

/**
 * A {@link Mission} is the smallest executable entity recognized by MolR
 *
 * @author nachivpn
 * @author yassine-kr
 */
public interface Mission extends Serializable {

    /**
     * The class name of the mole which has the responsibility to run the mission
     *
     * @return the mole class name
     */
    String getMoleClassName();

    /**
     * The mission name, it is the reference to the mission code managed by the infrastructure layer
     *
     * @return the mission name
     */
    String getMissionName();

}