/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.commons.mission;

import cern.molr.commons.exception.MissionMaterializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Implementation of {@link MissionMaterializer} that can instantiate {@link Mission}s from {@link Class}es
 * annotated with {@link RunWithMole}
 *
 * @author tiagomr
 * @author yassine-kr
 */
public class AnnotatedMissionMaterializer implements MissionMaterializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotatedMissionMaterializer.class);

    private static Mole instantiateMole(final Class<? extends Mole> moleClass)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        try {
            Constructor<? extends Mole> constructor = moleClass.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | InvocationTargetException error) {
            LOGGER.error("Could not instantiate Mole of class [{}]", moleClass);
            throw error;
        }
    }

    @Override
    public Mission materialize(Class<?> classType) throws MissionMaterializationException {
        if (null == classType) {
            throw new MissionMaterializationException(new IllegalArgumentException("Class type cannot be null"));
        }
        LOGGER.info("Materializing annotated mission class [{}]", classType.getCanonicalName());
        RunWithMole moleAnnotation = classType.getAnnotation(RunWithMole.class);
        if (null == moleAnnotation) {
            throw new MissionMaterializationException(new IllegalArgumentException(String.format("Class type [%s] is " +
                    "not annotated with RunWithMole", classType)));
        }
        Class<? extends Mole> moleClass = moleAnnotation.value();
        LOGGER.debug("Annotation RunWithMole found with mole class [{}]", moleClass.getCanonicalName());
        try {
            Mole<?, ?> mole = instantiateMole(moleClass);
            LOGGER.debug("Mole class instantiated");
            String moleClassName = moleClass.getName();
            LOGGER.debug("Running mole discovery method");
            mole.verify(classType);
            Mission mission = new MissionImpl(moleClassName, classType.getName());
            LOGGER.debug("Mission created [{}]", mission);
            return mission;
        } catch (Exception error) {
            throw new MissionMaterializationException(error);
        }
    }
}
