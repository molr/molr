/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.domain;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicIncrementMissionHandleFactory implements MissionHandleFactory {

    private final AtomicLong nextId;
    private final Object target;

    public AtomicIncrementMissionHandleFactory(Object target) {
        this.target = target;
        nextId = new AtomicLong(0);
    }

    @Override
    public MissionHandle createHandle() {
        String className = target.getClass().getName();
        String hashCode = "" + System.identityHashCode(target);
        String nextId = "" + this.nextId.getAndIncrement();
        return MissionHandle.ofId(String.format("%s::%s::%s", className, hashCode, nextId));
    }

}
