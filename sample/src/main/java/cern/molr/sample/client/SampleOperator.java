/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.sample.client;

import java.util.concurrent.CompletableFuture;

import cern.molr.mission.run.RunMissionController;
import cern.molr.mission.service.MissionExecutionService;
import cern.molr.type.Ack;
/**
 * Sample implementation and usage of the operator's interfaces to demonstrate communication
 * 
 * @author nachivpn
 * @author yassine
 */
public class SampleOperator {

    /**
     * Usage of MolR by the operator client (the request-response will be done under the hood later)
     */

    MissionExecutionService mExecService;

    public SampleOperator( MissionExecutionService mExecService) {
        this.mExecService = mExecService;
    }
    
    public void operatorRun1() throws Exception{
        CompletableFuture<RunMissionController<Void>> futureController = mExecService.<Void, Void>runToCompletion("cern.molr.sample.mission.RunnableHelloWriter", null, Void.class, Void.class);
        try {
            RunMissionController<Void> controller = futureController.get();
            CompletableFuture<Void> futureResult = controller.getResult();
            futureResult.get();
        } catch (Exception e) {
            //handle
            System.err.println("Operator received an error:");
            e.printStackTrace();
            throw e;
        }
    }
    
    public Integer operatorRun2() throws Exception{
        CompletableFuture<RunMissionController<Integer>> futureController = 
                mExecService.<Integer, Integer>runToCompletion("cern.molr.sample.mission.IntDoubler", 21, Integer.class, Integer.class);
        try {
            RunMissionController<Integer> controller = futureController.get();
            CompletableFuture<Integer> futureResult = controller.getResult();
            return futureResult.get();
        } catch (Exception e) {
            //handle
            System.err.println("Operator received an error:");
            e.printStackTrace();
            throw e;
        }
        
    }
    
    public Integer operatorRun3() throws Exception{
        CompletableFuture<RunMissionController<Integer>> futureController = 
                mExecService.<Integer, Integer>runToCompletion("cern.molr.sample.mission.Fibonacc",
                        42, Integer.class, Integer.class);

        try {
            RunMissionController<Integer> controller = futureController.get();
            CompletableFuture<Ack> cancelResult = controller.cancel();
            System.out.println(cancelResult.get().getMessage());
            CompletableFuture<Integer> futureResult = controller.getResult();
            return futureResult.get();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;     
        }
    }
    
    
}
