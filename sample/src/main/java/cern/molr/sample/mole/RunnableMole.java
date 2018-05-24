/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.sample.mole;


import cern.molr.exception.IncompatibleMissionException;
import cern.molr.exception.MissionExecutionException;
import cern.molr.mission.Mission;
import cern.molr.mole.Mole;

/**
 * Implementation of {@link Mole} which allows for the execution of classes implementing the
 * {@link Runnable} interface.
 *
 * @author tiagomr
 * @author nachivpn
 * @author yassine
 * @see Mole
 */
public class RunnableMole implements Mole<Void,Void> {

    @Override
    public void verify(Class<?> classType) throws IncompatibleMissionException {
        if (null == classType) {
            throw new IllegalArgumentException("Class type cannot be null");
        }
        if (Runnable.class.isAssignableFrom(classType)) {
            try {
                classType.getMethod("run");
            } catch (NoSuchMethodException e) {
                throw new IncompatibleMissionException(e);
            }
        }else
            throw new IncompatibleMissionException("Mission must implement Runnable interface");
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
