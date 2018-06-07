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

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Implementation of {@link Mole} which allows for the execution of classes implementing the
 * {@link Function<Integer,Integer>} interface.
 *
 * @author nachivpn
 * @author yassine-kr
 * @see Mole
 */
public class IntegerFunctionMole implements Mole<Integer, Integer> {

    @Override
    public void verify(Class<?> classType) throws IncompatibleMissionException {
        if (null == classType) {
            throw new IllegalArgumentException("Class type cannot be null");
        }
        if (Function.class.isAssignableFrom(classType)) {
            try {
                Method m = classType.getMethod("apply", Integer.class);
                if (m.getReturnType() != Integer.class)
                    throw new IncompatibleMissionException("Mission must implement IntFunction interface");
            } catch (NoSuchMethodException error) {
                throw new IncompatibleMissionException(error);
            }
        } else
            throw new IncompatibleMissionException("Mission must implement IntFunction interface");
    }

    @Override
    public Integer run(Mission mission, Integer arg) throws MissionExecutionException {
        try {
            @SuppressWarnings("unchecked")
            Class<Function<Integer, Integer>> missionClass =
                    (Class<Function<Integer, Integer>>) Class.forName(mission.getMissionName());
            Function<Integer, Integer> missionInstance = missionClass.getConstructor().newInstance();
            return missionInstance.apply(arg);
        } catch (Exception error) {
            throw new MissionExecutionException(error);
        }
    }

}
