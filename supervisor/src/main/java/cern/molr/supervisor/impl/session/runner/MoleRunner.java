/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.impl.session.runner;

import cern.molr.commons.commands.Start;
import cern.molr.commons.commands.Terminate;
import cern.molr.commons.events.*;
import cern.molr.commons.exception.MissionExecutionException;
import cern.molr.commons.mission.Mission;
import cern.molr.commons.mission.MissionImpl;
import cern.molr.commons.mission.Mole;
import cern.molr.commons.request.MissionCommand;
import cern.molr.commons.response.ManuallySerializable;
import cern.molr.commons.response.MissionEvent;
import cern.molr.supervisor.api.session.runner.CommandListener;
import cern.molr.supervisor.api.session.runner.MoleRunnerState;
import cern.molr.supervisor.impl.session.CommandStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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


    private Mission mission;
    private Object missionInput;
    private Class<?> missionInputClass;
    private CommandsReader reader;
    private MoleRunnerState moleRunnerState = new MoleRunnerStateImpl();

    public MoleRunner(String argumentString) {

        ObjectMapper mapper = new ObjectMapper();

        try {

            MoleRunnerArgument argument = mapper.readValue(argumentString, MoleRunnerArgument.class);

            /*de-serialize mission*/
            mission = mapper.readValue(argument.getMissionObjString(), MissionImpl.class);

            /*de-serialize mission arg*/
            missionInputClass = Class.forName(argument.getMissionInputClassName());
            missionInput = mapper.readValue(argument.getMissionInputObjString(), missionInputClass);

            reader = new CommandsReader(new BufferedReader(new InputStreamReader(System.in)), this);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("The MoleRunner#main must receive at least 2 arguments, being them" +
                    " the fully qualified domain name of the Mole to be used and the fully qualified domain name of " +
                    "the Mission to be executed");
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        System.out.println(mapper.writeValueAsString(new SessionInstantiated()));
        new MoleRunner(args[0]);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            MissionEvent jvmDestroyedEvent = new SessionTerminated();
            try {
                System.out.println(mapper.writeValueAsString(jvmDestroyedEvent));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }));
        while (true) ;
    }

    /**
     * Start execution of the mission
     */
    private void startMission() {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        try {

            Mole<Object, Object> mole = createMoleInstance(mission.getMoleClassName());
            mole.verify(Class.forName(mission.getMissionDefnClassName()));

            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return mole.run(mission, missionInput);
                } catch (MissionExecutionException e) {
                    throw new CompletionException(e);
                }
            });

            MissionEvent missionStartedEvent =
                    new MissionStarted(
                            mission.getMissionDefnClassName(), missionInput, mission.getMoleClassName());
            System.out.println(mapper.writeValueAsString(missionStartedEvent));

            moleRunnerState.changeState();

            CompletableFuture<Void> future2 = CompletableFuture.supplyAsync(() -> {
                try {
                    MissionEvent missionFinishedEvent =
                            new MissionFinished(mission.getMissionDefnClassName(),
                                    future.get(), mission.getMoleClassName());
                    System.out.println(mapper.writeValueAsString(missionFinishedEvent));
                    System.exit(0);
                    return null;
                } catch (JsonProcessingException e) {
                    try {
                        System.out.println(mapper.writeValueAsString(new MissionException(e)));
                    } catch (JsonProcessingException e1) {
                        System.out.println(ManuallySerializable.serializeArray(
                                new MissionException("unable to serialize a mission exception")));
                    }
                } catch (ExecutionException | InterruptedException e) {
                    try {
                        System.out.println(mapper.writeValueAsString(new MissionException(e.getCause())));
                    } catch (JsonProcessingException e1) {
                        System.out.println(ManuallySerializable.serializeArray(
                                new MissionException("unable to serialize a mission exception")));
                    }
                }
                return null;
            });
        } catch (Exception e) {
            try {
                System.out.println(mapper.writeValueAsString(new MissionException(e)));
            } catch (JsonProcessingException e1) {
                System.out.println(ManuallySerializable.serializeArray(
                        new MissionException("unable to serialize a mission exception")));
            }
        }
    }

    /**
     * send command to the mission
     */
    private void sendComand() {

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
            moleRunnerState.acceptCommand(command);

            CommandStatus commandStatus = new CommandStatus(true,
                    "command accepted by the Mole runner");
            System.out.println(mapper.writeValueAsString(commandStatus));

            if (command instanceof Start)
                startMission();
            else if (command instanceof Terminate)
                terminate();

        } catch (Exception e) {
            try {
                CommandStatus commandStatus = new CommandStatus(e);
                System.out.println(mapper.writeValueAsString(commandStatus));
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
                System.out.println(ManuallySerializable.serializeArray(
                        new CommandStatus("unable to serialize a failure status")));
            }
        }
    }
}