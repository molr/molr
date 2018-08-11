/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.api.domain;

import java.util.Set;

public interface MissionState {

    /**
     * Retrieves the allowed commands for the given strand.
     *
     * @param strand the strand for which to query the allowed commands
     * @return a set of commands that are currently allowed.
     */
    Set<MissionCommand> allowedCommandsFor(Strand strand);

    Block cursorPositionIn(Strand strand);

    RunState runStateOf(Strand strand);

    Set<Strand> activeStrands();

}
