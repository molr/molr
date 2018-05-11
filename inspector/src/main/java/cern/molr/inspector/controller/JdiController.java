/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“.ing this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.controller;

import cern.molr.mission.controller.custom.StepResult;

/**
 * A controller for a JDI instance that can control any running JVM instances by stepping through or terminating them.
 * NOTE: The main difference between {@link JdiController} and {@link cern.molr.mole.supervisor.JdiController} is that it does not return a {@link StepResult}
 * The choice of which is more appropriate needs to be made
 * 
 */
public interface JdiController {

    /**
     * Takes one <i>step</i> in an entry by executing one line / instruction in the running JVM.
     */
    void stepForward();

    /**
     * Resume the running JVM session, not stepping nor stopping anymore
     */
    void resume();

    /**
     * Terminates the running JVM session.
     */
    void terminate();

}
