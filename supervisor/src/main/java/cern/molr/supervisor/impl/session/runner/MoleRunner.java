/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.impl.session.runner;

import cern.molr.commons.api.exception.CommandNotAcceptedException;
import cern.molr.commons.api.exception.MissionExecutionException;
import cern.molr.commons.api.mission.Mission;
import cern.molr.commons.api.mission.Mole;
import cern.molr.commons.api.mission.StateManagerListener;
import cern.molr.commons.api.request.MissionCommand;
import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.api.response.MissionState;
import cern.molr.commons.api.web.SimpleSubscriber;
import cern.molr.commons.commands.MissionControlCommand;
import cern.molr.commons.events.*;
import cern.molr.commons.impl.mission.MissionImpl;
import cern.molr.supervisor.api.session.runner.CommandListener;
import cern.molr.commons.api.mission.StateManager;
import cern.molr.supervisor.impl.session.CommandStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

/**
 * The entry point to execute a mission in a spawned JVM. It has a reader which reads commands from STDIN and writes
 * events
 * in STDOUT
 *
 * @author nachivpn
 * @author yassine-kr
 */
public class MoleRunner implements CommandListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoleRunner.class);

    private Mission mission;
    private Object missionInput;
    private Class<?> missionInputClass;
    private CommandsReader reader;
    private StateManager stateManager = new MoleRunnerStateManager();
    private Mole<Object, Object> mole;
    private ObjectMapper mapper;

    public MoleRunner(String argumentString) {

        mapper = new ObjectMapper();

        try {

            MoleRunnerArgument argument = mapper.readValue(argumentString, MoleRunnerArgument.class);

            /*de-serialize mission*/
            mission = mapper.readValue(argument.getMissionObjString(), MissionImpl.class);

            /*de-serialize mission arg*/
            missionInputClass = Class.forName(argument.getMissionInputClassName());
            missionInput = mapper.readValue(argument.getMissionInputObjString(), missionInputClass);

            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

            stateManager.addListener(() -> sendStateEvent(new MissionState(MissionState.Level.MOLE_RUNNER, stateManager.getStatus(),
                    stateManager.getPossibleCommands())));

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                sendEvent(new MissionControlEvent(MissionControlEvent.Event.SESSION_TERMINATED));
            }));

            sendEvent(new MissionControlEvent(MissionControlEvent.Event.SESSION_INSTANTIATED));

            reader = new CommandsReader(new BufferedReader(new InputStreamReader(System.in)), this);

        } catch (Exception error) {
            LOGGER.error("error while initializing the session", error);
            System.exit(-1);
        }
    }

    /**
     * Method which writes an event to the output stream
     */
    private void sendEvent(MissionEvent event) {
        stateManager.changeState(event);
        try {
            System.out.println(mapper.writeValueAsString(event));
        } catch (JsonProcessingException error) {
            LOGGER.error("unable to serialize an event", error);
        }
    }

    /**
     * Method which writes a state wrapped in an event
     */
    private void sendStateEvent(MissionState state) {
        try {
            System.out.println(mapper.writeValueAsString(new MissionStateEvent(state)));
        } catch (JsonProcessingException error) {
            LOGGER.error("unable to serialize a state event", error);
        }
    }


    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("The MoleRunner#main must receive at least 2 arguments, being them" +
                    " the fully qualified domain name of the Mole to be used and the fully qualified domain name of " +
                    "the Mission to be executed");
        }
        new MoleRunner(args[0]);
        
        while (true) ;
    }

    /**
     * Start execution of the mission
     */
    private void startMission() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        try {

            mole = createMoleInstance(mission.getMoleClassName());
            mole.verify(mission.getMissionName());

            if (mole.getEventsPublisher() != null) {
                mole.getEventsPublisher().subscribe(new SimpleSubscriber<MissionEvent>() {
                    @Override
                    public void consume(MissionEvent event) {
                        sendEvent(event);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            if (mole.getStatesPublisher() != null) {
                mole.getStatesPublisher().subscribe(new SimpleSubscriber<MissionState>() {
                    @Override
                    public void consume(MissionState state) {
                        sendStateEvent(state);
                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }


            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return mole.run(mission, missionInput);
                } catch (MissionExecutionException error) {
                    throw new CompletionException(error);
                }
            });

            sendEvent(new MissionControlEvent(MissionControlEvent.Event.MISSION_STARTED));

            CompletableFuture<Void> future2 = CompletableFuture.supplyAsync(() -> {
                try {
                    MissionEvent missionFinishedEvent =
                            new MissionFinished(mission.getMissionName(), future.get());
                    sendEvent(missionFinishedEvent);
                } catch (ExecutionException | InterruptedException error) {
                    sendEvent(new MissionExceptionEvent(error.getCause()));
                }
                System.exit(0);
                return null;
            });
        } catch (Exception error) {
            sendEvent(new MissionExceptionEvent(error));
        }
    }

    /**
     * kill JVM
     */
    private void terminate() {
        System.exit(0);
    }

    private <I, O> Mole<I, O> createMoleInstance(String moleName) throws Exception {
        @SuppressWarnings("unchecked")
        Class<Mole<I, O>> clazz = (Class<Mole<I, O>>) Class.forName(moleName);
        return clazz.getConstructor().newInstance();
    }

    @Override
    public void onCommand(MissionCommand command) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        try {
            if (command instanceof MissionControlCommand) {
                stateManager.acceptCommand(command);

                CommandStatus commandStatus = new CommandStatus(true,
                        "command accepted by the MoleRunner");
                sendEvent(commandStatus);

                MissionControlCommand c = (MissionControlCommand) command;
                switch (c.getCommand()) {
                    case START:
                        startMission();
                        break;
                    case TERMINATE:
                        terminate();
                        break;
                }
            } else {
                mole.sendCommand(command);

                CommandStatus commandStatus = new CommandStatus(true,
                        "command accepted by the Mole");
                sendEvent(commandStatus);
            }


        } catch (CommandNotAcceptedException error) {
            CommandStatus commandStatus = new CommandStatus(error);
            sendEvent(commandStatus);
        }
    }
}