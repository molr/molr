/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.mission;

import java.io.Serializable;

/**
 * A {@link Mission} is the smallest executable entity recognized by MolR
 * 
 * @author nachivpn
 */
public interface Mission extends Serializable{

    String getMoleClassName();

    String getMissionDefnClassName();

}