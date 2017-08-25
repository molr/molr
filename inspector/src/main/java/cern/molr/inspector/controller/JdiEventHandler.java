/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“.ing this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.controller;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import org.jdiscript.handlers.BaseEventHandler;

/**
 * An event handler for the {@link JdiController} that keeps track of references spawned in the running JVM and
 * that extends the {@link BaseEventHandler} for receiving callbacks from JDI.
 */
public abstract class JdiEventHandler extends BaseEventHandler {

    /**
     * Creates an event handler that receives callbacks from the given virtual machine.
     *
     * @param vm The virtual machine this event handler interacts with.
     */
    public JdiEventHandler(VirtualMachine vm) {
        super(vm);
    }

    /**
     * Searches for active debugging sessions in the running JVM and returns a {@link ThreadReference}, describing
     * the thread actively running the requested class - if one is found.
     *
     * @param className The name of the class to search for.
     * @return A {@link ThreadReference} instance if the class has been instrumented and is still running.
     * <code>Null</code> otherwise.
     */
    public abstract ThreadReference getReferenceForClass(String className);

}
