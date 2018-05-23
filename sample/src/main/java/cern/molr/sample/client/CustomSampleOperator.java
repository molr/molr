/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.sample.client;

import java.util.concurrent.CompletableFuture;

import cern.molr.mission.controller.custom.CustomRunMissionController;
import cern.molr.mission.service.custom.CustomMissionExecutionService;
import cern.molr.type.Ack;
/**
 * Sample implementation and usage of the custom interface to control a mission execution
 * TODO test this operator after implementing the custom service
 * 
 * @author nachivpn
 * @author yassine-kr
 */
public class CustomSampleOperator {

    /**
     * Usage of MolR by the operator client (the request-response will be done under the hood later)
     */

    CustomMissionExecutionService mExecService;

    public CustomSampleOperator(CustomMissionExecutionService mExecService) {
        this.mExecService = mExecService;
    }
    
    public void operatorRun1() throws Exception{
        CompletableFuture<CustomRunMissionController<Void>> futureController = mExecService.<Void, Void>runToCompletion("cern.molr.sample.mission.RunnableHelloWriter", null, Void.class, Void.class);
        try {
            CustomRunMissionController<Void> controller = futureController.get();
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
        CompletableFuture<CustomRunMissionController<Integer>> futureController =
                mExecService.<Integer, Integer>runToCompletion("cern.molr.sample.mission.IntDoubler", 21, Integer.class, Integer.class);
        try {
            CustomRunMissionController<Integer> controller = futureController.get();
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
        CompletableFuture<CustomRunMissionController<Integer>> futureController =
                mExecService.<Integer, Integer>runToCompletion("cern.molr.sample.mission.Fibonacc",
                        42, Integer.class, Integer.class);

        try {
            CustomRunMissionController<Integer> controller = futureController.get();
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
