/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“.ing this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.supervisor.impl.spawner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Abstract class that encapsulates the behaviour for spawning a new JVM using the {@link ProcessBuilder}
 *
 * @author tiagomr
 */
public final class JvmSpawnHelper {

    public static final String JAVA_HOME = System.getProperty("java.home");
    public static final String TOOLS_PATH = String.format("%s/../lib/tools.jar", JAVA_HOME);
    private static final Logger LOGGER = LoggerFactory.getLogger(JvmSpawnHelper.class);
    private static final String CLASSPATH_ARGUMENT_INDICATOR = "-cp";

    private JvmSpawnHelper() {
        // This class should not be instantiated
    }

    public static final ProcessBuilder getProcessBuilder(String classpath, String mainClass, String... arguments)
            throws IOException {
        List<String> command = new ArrayList<>();
        command.add(String.format("%s/bin/java", JAVA_HOME));
        command.add(CLASSPATH_ARGUMENT_INDICATOR);
        command.add(classpath);
        command.add(mainClass);
        if (arguments != null) {
            command.addAll(rectifyArgs(arguments));
        }
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        LOGGER.info("Starting JVM with parameters: [{}]", command.toString());
        return processBuilder;
    }

    private static List<String> rectifyArgs(String[] arguments) {
        if (System.getProperty("os.name").startsWith("Windows")) {
            return Arrays.stream(arguments).map(JvmSpawnHelper::asWindowsArgument).collect(toList());
        } else {
            return Arrays.asList(arguments);
        }
    }

    public static final String appendToolsJarToClasspath(String classpath) {
        if (null == classpath) {
            throw new IllegalArgumentException("Classpath cannot be null");
        }
        if (classpath.contains("tools.jar")) {
            return classpath;
        }
        return String.format("%s:%s", classpath, TOOLS_PATH);
    }

    /**
     * Replaces all required characters so that a string can be passed as an argument to the process builder.
     *
     * @param aString a string that shall be rectified to be used an argument for passing as an argument to a process builer
     * @return the string as a valid argument for a process builder
     */
    public static final String asWindowsArgument(String aString) {
        return aString.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
