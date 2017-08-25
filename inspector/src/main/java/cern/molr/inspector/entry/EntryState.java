/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“.ing this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.entry;

/**
 * The state of a single entry running in JDI (Java Debugging Interface). An entry is defined as a single method in
 * a class that gets executed from top to bottom.
 */
public interface EntryState {

    /**
     * The full name of the class relative to the root class path.
     * @return A {@link String} containing the full package and class name of the class currently being run.
     */
    String getClassName();

    /**
     * The name of the method inside the class that is being run.
     * @return A {@link String} containing the name of the method without any parameters.
     */
    String getMethodName();

    /**
     * Returns the position in terms of lines in the source code (starting with line 1 in the beginning) of the file
     * where the execution currently rests.
     * @return A number between 1 and the length of the source file that is being executed.
     */
    int getLine();

}
