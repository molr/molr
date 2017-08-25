/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.jdi;

import com.sun.jdi.ClassType;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.event.ClassPrepareEvent;
import org.jdiscript.handlers.OnClassPrepare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * An {@link OnClassPrepare} implementation which keeps track of new implementations of a given class (or interface).
 * Whenever a class that has not been initialised before is added, the callback given in the constructor is called.
 * This class is not thread safe.
 */
public class ClassInstantiationListener implements OnClassPrepare {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassInstantiationListener.class);

    private final Consumer<ClassType> implementorCallback;
    private final String implementorClass;
    private Set<ClassType> currentImplementations = new HashSet<>();

    /**
     * Creates a new listener instructed to search for implementations of the given class. The callback will be
     * called everytime a class or subclass is instantiated that has <i>not</i> been instantiated - i. e. created
     * for the first time.
     *
     * @param implementorClass    The class or interface to search for.
     * @param implementorCallback A callback to call whenever a new (unique) class instance is created.
     */
    public ClassInstantiationListener(String implementorClass, Consumer<ClassType> implementorCallback) {
        this.implementorClass = implementorClass;
        this.implementorCallback = implementorCallback;
    }

    @Override
    public void classPrepare(ClassPrepareEvent event) {
        final ReferenceType referenceType = event.referenceType();
        if (referenceType == null) {
            LOGGER.warn("No reference type defined in class preparation event: {}", event);
        } else if (referenceType instanceof ClassType) {
            final ClassType classType = (ClassType) referenceType;
            if (isClassEquals(classType, implementorClass) && !currentImplementations.contains(classType)) {
                currentImplementations.add(classType);
                implementorCallback.accept(classType);
            }
        }
    }

    /**
     * Tests if the given {@link ClassType} is representing the same class as the given {@link Class} by comparing their
     * full path names.
     *
     * @param type The class type to test.
     * @return True if the classes are considered the same.
     */
    static boolean isClassEquals(ClassType type, String className) {
        return type.name().equals(className);
    }

}
