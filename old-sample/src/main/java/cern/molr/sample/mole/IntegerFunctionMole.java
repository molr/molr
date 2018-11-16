/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“. In applying this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.sample.mole;


import cern.molr.commons.api.exception.CommandNotAcceptedException;
import cern.molr.commons.api.exception.IncompatibleMissionException;
import cern.molr.commons.api.exception.MissionExecutionException;
import cern.molr.commons.api.exception.MissionResolvingException;
import cern.molr.commons.api.mission.Mission;
import cern.molr.commons.api.mission.Mole;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import cern.molr.commons.impl.mission.MissionServices;
import org.reactivestreams.Publisher;

import java.lang.reflect.Method;
import java.util.function.Function;

/**
 * Implementation of {@link Mole} which allows for the execution of classes implementing the
 * {@link Function<Integer, Integer>} interface.
 *
 * @author nachivpn
 * @author yassine-kr
 * @see Mole
 */
public class IntegerFunctionMole implements Mole<Integer, Integer> {

    @Override
    public void verify(String missionName) throws IncompatibleMissionException {
        Class<?> classType = null;
        try {
            classType = MissionServices.getResolver().resolve(missionName);
        } catch (MissionResolvingException error) {
            throw new IncompatibleMissionException(error);
        }

        if (null == missionName) {
            throw new IllegalArgumentException("Class type cannot be null");
        }
        if (Function.class.isAssignableFrom(classType)) {
            try {
                Method m = classType.getMethod("apply", Integer.class);
                if (m.getReturnType() != Integer.class) {
                    throw new IncompatibleMissionException("Mission must implement IntFunction interface");
                }
            } catch (NoSuchMethodException error) {
                throw new IncompatibleMissionException(error);
            }
        } else
            throw new IncompatibleMissionException("Mission must implement IntFunction interface");
    }

    @Override
    public Integer run(Mission mission, Integer missionArguments) throws MissionExecutionException {
        try {
            @SuppressWarnings("unchecked")
            Class<Function<Integer, Integer>> missionClass =
                    (Class<Function<Integer, Integer>>) MissionServices.getResolver().resolve(mission.getMissionName());
            Function<Integer, Integer> missionInstance = missionClass.getConstructor().newInstance();
            return missionInstance.apply(missionArguments);
        } catch (Exception error) {
            throw new MissionExecutionException(error);
        }
    }

    /**
     * This mole does not define specific commands
     * @param command
     * @throws CommandNotAcceptedException
     */
    @Override
    public void sendCommand(MissionCommand command) throws CommandNotAcceptedException {

    }

    @Override
    public Publisher<MissionEvent> getEventsPublisher() {
        return null;
    }

    @Override
    public Publisher<MissionState> getStatesPublisher() {
        return null;
    }

}
