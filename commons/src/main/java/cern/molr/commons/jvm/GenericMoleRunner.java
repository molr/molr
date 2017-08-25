/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.commons.jvm;

import com.fasterxml.jackson.databind.ObjectMapper;

import cern.molr.commons.MissionImpl;
import cern.molr.mole.Mole;

/**
 * {@link GenericMoleRunner} Executes a mission & and writes output to STDOUT.
 * NOT used currently, but needed to implement stepping (and eventually maybe even running) 
 * as the entry point to executing {@link cern.molr.commons.domain.Mission}s.
 * 
 * @author nachivpn
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
            
            /*instantiate and run mole*/
            Mole<Object,Object> mole = createMoleInstance(mission.getMoleClassName());
            Object missionOutput = mole.run(mission, missionInput);
            
            /*serialize output & write it to stdout*/
            String missionOutputString = mapper.writeValueAsString(missionOutput);
            System.out.print(missionOutputString);
            
        } catch (Exception e) {
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