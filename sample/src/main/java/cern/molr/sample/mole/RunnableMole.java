/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.sample.mole;


import cern.molr.commons.exception.IncompatibleMissionException;
import cern.molr.commons.exception.MissionExecutionException;
import cern.molr.commons.mission.Mission;
import cern.molr.commons.mission.Mole;

/**
 * Implementation of {@link Mole} which allows for the execution of classes implementing the
 * {@link Runnable} interface.
 *
 * @author tiagomr
 * @author nachivpn
 * @author yassine-kr
 * @see Mole
 */
public class RunnableMole implements Mole<Void, Void> {

    @Override
    public void verify(Class<?> classType) throws IncompatibleMissionException {
        if (null == classType) {
            throw new IllegalArgumentException("Class type cannot be null");
        }
        if (Runnable.class.isAssignableFrom(classType)) {
            try {
                classType.getMethod("run");
            } catch (NoSuchMethodException error) {
                throw new IncompatibleMissionException(error);
            }
        } else
            throw new IncompatibleMissionException("Mission must implement Runnable interface");
    }

    @Override
    public Void run(Mission mission, Void args) throws MissionExecutionException {
        try {
            Class<?> missionClass = Class.forName(mission.getMissionName());
            Object missionInstance = missionClass.getConstructor().newInstance();
            if (!(missionInstance instanceof Runnable)) {
                throw new IllegalArgumentException(String
                        .format("Mission content class must implement the %s interface", Runnable.class.getName()));
            }
            ((Runnable) missionInstance).run();
        } catch (Exception error) {
            throw new MissionExecutionException(error);
        }
        return null;
    }

}
