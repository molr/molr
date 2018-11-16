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
import cern.molr.commons.api.mission.StateManager;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import cern.molr.commons.impl.mission.MissionServices;
import cern.molr.sample.commands.SequenceCommand;
import cern.molr.sample.events.SequenceMissionEvent;
import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.ReplayProcessor;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Implementation of {@link Mole} which allows for the execution of classes implementing the {@link SequenceMission}
 * interface.
 * <p>
 * It runs the mission tasks consecutively
 *
 * @author yassine-kr
 * @see Mole
 */
public class SequenceMole implements Mole<Void, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequenceMole.class);

    private List<SequenceMission.Task> tasks;
    private int currentTask = 0;
    private CountDownLatch endSignal = new CountDownLatch(1);
    private Processor<MissionEvent, MissionEvent> eventsProcessor = DirectProcessor.create();
    private Processor<MissionState, MissionState> statesProcessor = ReplayProcessor.cacheLast();
    private StateManager stateManager;
    private boolean pause;//Whether the mole has received a PAUSE command

    @Override
    public void verify(String missionName) throws IncompatibleMissionException {
        Class<?> classType = null;
        try {
            classType = MissionServices.getResolver().resolve(missionName);
        } catch (MissionResolvingException error) {
            throw new IncompatibleMissionException(error);
        }

        if (null == classType) {
            throw new IllegalArgumentException("Class type cannot be null");
        }
        if (SequenceMission.class.isAssignableFrom(classType)) {
            try {
                classType.getMethod("getTasks");
            } catch (NoSuchMethodException error) {
                throw new IncompatibleMissionException(error);
            }
        } else
            throw new IncompatibleMissionException("Mission must implement SequenceMission interface");
    }

    @Override
    public Void run(Mission mission, Void missionArguments) throws MissionExecutionException {
        try {
            Class<?> missionClass = MissionServices.getResolver().resolve(mission.getMissionName());
            Object missionInstance = missionClass.getConstructor().newInstance();
            if (!(missionInstance instanceof SequenceMission)) {
                throw new IllegalArgumentException(String
                        .format("Mission content class must implement the %s interface", SequenceMission.class.getName()));
            }
            tasks = ((SequenceMission) missionInstance).getTasks();
            if (tasks == null || tasks.size() == 0) {
                throw new IllegalArgumentException("Null or Empty tasks list");
            }
            stateManager = new SequenceMoleStateManager(tasks.size());
            stateManager.addListener(() -> {
                statesProcessor.onNext(stateManager.getState());
            });

            statesProcessor.onNext(stateManager.getState());
            endSignal.await();
        } catch (Exception error) {
            throw new MissionExecutionException(error);
        }
        return null;
    }

    @Override
    public void sendCommand(MissionCommand command) throws CommandNotAcceptedException {
        LOGGER.info("Command {} arrived.", command);
        stateManager.acceptCommand(command);

        switch (((SequenceCommand) command).getCommand()) {
            case STEP:
                new Thread(this::runTask).start();
                break;
            case SKIP:
                stateManager.changeState(new SequenceMissionEvent(currentTask, SequenceMissionEvent.Event.TASK_SKIPPED, ""));
                nextTask();
                break;
            case RESUME:
                pause = false;
                new Thread(this::runTasks).start();
                break;
            case PAUSE:
                pause = true;
                break;
        }
    }

    private void nextTask() {
        currentTask++;
        if (currentTask == tasks.size()) {
            endSignal.countDown();
        }
    }

    private void runTask() {
        MissionEvent event = new SequenceMissionEvent(currentTask, SequenceMissionEvent.Event.TASK_STARTED, "");
        stateManager.changeState(event);
        eventsProcessor.onNext(event);

        try {
            tasks.get(currentTask).run();
            event = new SequenceMissionEvent(currentTask, SequenceMissionEvent.Event.TASK_FINISHED, "");
        } catch (Exception error) {
            event = new SequenceMissionEvent(currentTask, error, error.getMessage());
        }
        stateManager.changeState(event);
        eventsProcessor.onNext(event);

        nextTask();
    }

    private void runTasks() {
        MissionEvent event = new SequenceMissionEvent(currentTask, SequenceMissionEvent.Event.RESUMED, "");
        stateManager.changeState(event);
        do {
            runTask();
        } while (!pause && currentTask < tasks.size());
        event = new SequenceMissionEvent(currentTask, SequenceMissionEvent.Event.PAUSED, "");
        stateManager.changeState(event);
    }

    @Override
    public Publisher<MissionEvent> getEventsPublisher() {
        return eventsProcessor;
    }

    @Override
    public Publisher<MissionState> getStatesPublisher() {
        return statesProcessor;
    }

}
