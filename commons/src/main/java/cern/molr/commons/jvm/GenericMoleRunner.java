/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.jvm;

import cern.molr.exception.MissionExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cern.molr.commons.MissionImpl;
import cern.molr.mole.Mole;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * {@link GenericMoleRunner} Executes a mission & and writes output to STDOUT.
 * NOT used currently, but needed to implement stepping (and eventually maybe even running) 
 * as {@link GenericMoleRunner} serves as an entry point to executing {@link cern.molr.mission.Mission}s.
 * Using the {@link GenericMoleRunner} for starting a mission on supervisor,
 * would need some important changes such as sending the input and output class form the client to server in the request
 * TODO verify whether this class is relevant, maybe remove it
 * 
 * @author nachivpn
 * @author yassine
 */
public class GenericMoleRunner {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("The GenericMoleRunner#main must receive at least 2 arguments, being them" +
                    " the fully qualified domain name of the Mole to be used and the fully qualified domain name of the " +
                    "Mission to be executed");
        }

        String argumentString = args[0];

        ObjectMapper mapper = new ObjectMapper();
        
        try {

            GenericMoleRunnerArgument argument = mapper.readValue(argumentString, GenericMoleRunnerArgument.class);
            
            /*de-serialize mission*/
            MissionImpl mission = mapper.readValue(argument.getMissionObjString(), MissionImpl.class);
            
            /*de-serialize mission arg*/
            Class<?> missionInputClass = Class.forName(argument.getMissionInputClassName());
            Object missionInput = mapper.readValue(argument.getMissionInputObjString(), missionInputClass);
            
            /*instantiate and run a mole*/
            Mole<Object,Object> mole = createMoleInstance(mission.getMoleClassName());

            CompletableFuture<Object> future=CompletableFuture.supplyAsync(()->{
                try {
                    return mole.run(mission, missionInput);
                } catch (MissionExecutionException e) {
                    throw new CompletionException(e);
                }
            });

            try {
                /*serialize output & write it to stdout*/
                String missionOutputString = mapper.writeValueAsString(future.get());
                System.out.print(missionOutputString);
            }catch (Exception e){
                throw e.getCause();
            }


        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.exit(0);
    }

    private static <I, O> Mole<I,O> createMoleInstance(String moleName) throws Exception {
        @SuppressWarnings("unchecked")
        Class<Mole<I,O>> clazz = (Class<Mole<I,O>>) Class.forName(moleName);
        return clazz.getConstructor().newInstance();
    }
}