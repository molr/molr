/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.commons.impl.mission;

import cern.molr.commons.api.exception.MissionMaterializationException;
import cern.molr.commons.api.exception.MissionResolvingException;
import cern.molr.commons.api.mission.Mission;
import cern.molr.commons.api.mission.MissionMaterializer;
import cern.molr.commons.api.mission.Mole;
import cern.molr.commons.api.mission.RunWithMole;
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
    public Mission materialize(String missionName) throws MissionMaterializationException {

        Class<?> classType = null;
        try {
            classType = MissionServices.getResolver().resolve(missionName);
        } catch (MissionResolvingException error) {
            throw new MissionMaterializationException(error);
        }

        if (null == missionName) {
            throw new MissionMaterializationException(new IllegalArgumentException("Class type cannot be null"));
        }
        LOGGER.info("Materializing annotated mission class [{}]", classType.getCanonicalName());
        RunWithMole moleAnnotation = classType.getAnnotation(RunWithMole.class);
        if (null == moleAnnotation) {
            throw new MissionMaterializationException(new IllegalArgumentException(String.format("Class type [%s] is " +
                    "not annotated with RunWithMole", missionName)));
        }
        Class<? extends Mole> moleClass = moleAnnotation.value();
        LOGGER.debug("Annotation RunWithMole found with mole class [{}]", moleClass.getCanonicalName());
        try {
            Mole<?, ?> mole = instantiateMole(moleClass);
            LOGGER.debug("Mole class instantiated");
            String moleClassName = moleClass.getName();
            LOGGER.debug("Running mole verify method");
            mole.verify(missionName);
            Mission mission = new MissionImpl(moleClassName, missionName);
            LOGGER.debug("Mission created [{}]", mission);
            return mission;
        } catch (Exception error) {
            throw new MissionMaterializationException(error);
        }
    }
}
