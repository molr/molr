/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

/**
 * <h1>Inspecting</h1>
 * <p>
 * This package provides features to spawn a separate JVM and control the execution in the VM by starting and
 * stopping the execution at given points (breakpoints). The package uses the Java Debugging Interface (JDI) via
 * the JDI script implementation by Jason Fager
 * (<a href="https://github.com/jfager/jdiscript">JDI script on GitHub</a>).
 * </p>
 * <h2>Entries</h2>
 * <p>
 * Instead of inspecting full files or classes, this implementation restricts the inspection to methods. A method
 * registered for inspection is defined as an <i>entry</i>. Whenever such an entry is reached, the running VM is
 * suspended, and only continued when the {@link cern.molr.inspector.controller.JdiController} is requested to
 * resume the execution. The execution of the entry ends when the end of the method is reached.
 * </p>
 */
package cern.molr.inspector;