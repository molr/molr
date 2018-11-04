/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.domain;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicIncrementMissionHandleFactory implements MissionHandleFactory {

    private final AtomicLong nextId = new AtomicLong(0);

    @Override
    public MissionHandle createHandle() {
        return MissionHandle.ofId("" + nextId.getAndIncrement());
    }

}
