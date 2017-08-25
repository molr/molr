/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“.ing this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.domain;

import cern.molr.mission.Mission;

/**
 * A request to instantiate an inspector with a given classpath and a {@link Mission} to run.
 */
public interface InstantiationRequest {

    /**
     * Returns the full class path containing all the necessary libraries to run the request in a Java environment.
     *
     * @return A {@link String} containing zero or more classpaths, separated by {@link java.io.File#pathSeparator}.
     */
    String getClassPath();

    /**
     * The {@link Mission} that should be run with this request.
     *
     * @return A {@link Mission} containing information about what main class to run with what arguments.
     */
    Mission getMission();

}