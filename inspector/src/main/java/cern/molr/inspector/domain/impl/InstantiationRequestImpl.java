/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“.ing this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.domain.impl;

import cern.molr.inspector.domain.InstantiationRequest;
import cern.molr.mission.Mission;

/**
 * An immutable implementation of an {@link InstantiationRequest}.
 *
 * @author ?
 * @author yassine
 */
public class InstantiationRequestImpl implements InstantiationRequest {

    private final String classPath;
    private final Mission mission;
    private final Object inputObject;
    private final String inputClassName;

    /**
     * Creates a {@link InstantiationRequestImpl} using the given class path and {@link Mission}.
     *  @param classPath The class path containing zero or more paths separated by the {@link java.io.File#pathSeparator}.
     * @param mission   The mission to execute.
     * @param inputObject
     * @param inputClassName
     */
    public InstantiationRequestImpl(String classPath, Mission mission, Object inputObject, String inputClassName) {
        this.classPath = classPath;
        this.mission = mission;
        this.inputObject = inputObject;
        this.inputClassName = inputClassName;
    }

    @Override
    public String getClassPath() {
        return classPath;
    }

    @Override
    public Mission getMission() {
        return mission;
    }

    @Override
    public Object getInputObject() {
        return inputObject;
    }

    @Override
    public String getInputClassName() {
        return inputClassName;
    }

}