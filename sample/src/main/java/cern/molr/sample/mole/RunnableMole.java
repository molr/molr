/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.sample.mole;


import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import cern.molr.exception.IncompatibleMissionException;
import cern.molr.exception.MissionExecutionException;
import cern.molr.mission.Mission;
import cern.molr.mole.Mole;

/**
 * Implementation of {@link Mole} which allows for the discovery and execution of classes implementing the
 * {@link Runnable} interface.
 * <h3>Discovery:</h3> All classes annotated with {@link Runnable} are exposed as services.
 * <h3>Execution:</h3> Allows for the execution of the {@link Runnable#run()} entry point.
 *
 * @author tiagomr
 * @author nachivpn
 * @see Mole
 */
public class RunnableMole implements Mole<Void,Void> {

    @Override
    public List<Method> discover(Class<?> classType) throws IncompatibleMissionException {
        if (null == classType) {
            throw new IllegalArgumentException("Class type cannot be null");
        }
        if (Runnable.class.isAssignableFrom(classType)) {
            try {
                return Collections.singletonList(classType.getMethod("run"));
            } catch (NoSuchMethodException e) {
                throw new IncompatibleMissionException(e);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public Void run(Mission mission, Void args) throws MissionExecutionException {
        try {
            Class<?> missionContentClass = Class.forName(mission.getMissionDefnClassName());
            Object missionContentInstance = missionContentClass.getConstructor().newInstance();
            if (!(missionContentInstance instanceof Runnable)) {
                throw new IllegalArgumentException(String
                        .format("Mission content class must implement the %s interface", Runnable.class.getName()));
            }
            ((Runnable) missionContentInstance).run();
        } catch (Exception e) {
            throw new MissionExecutionException(e);
        }
        return null;
    }

}
