/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.api.domain;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public enum MissionCommand {
    PAUSE, STEP_OVER, STEP_INTO, RESUME;
}
