package cern.molr.sample.client;

import cern.molr.mission.service.adapter.*;

/**
 * Sample implementation and usage of the operator's interfaces to demonstrate communication using adapter api
 * 
 * @author yassine
 */
public class SampleOperatorAdapter {

    /**
     * Usage of MolR by the operator client (the request-response will be done under the hood later)
     */
    MissionExecutionServiceAdapter mExecService;

    public SampleOperatorAdapter(MissionExecutionServiceAdapter mExecService) {
        this.mExecService = mExecService;
    }

    public void operatorRun1() throws Exception{
        FutureRunMissionController<Void> futureController = mExecService.<Void, Void>runToCompletion("cern.molr.sample.mission.RunnableHelloWriter", null, Void.class, Void.class);
        try {
            RunMissionController<Void> controller = futureController.get();
            FutureRunMissionResult<Void> futureResult = controller.getResult();
            futureResult.get();
        } catch (Exception e) {
            //handle
            System.err.println("Operator received an error:");
            throw e;
        }
    }

    public Integer operatorRun2() throws Exception{
        FutureRunMissionController<Integer> futureController=
                mExecService.<Integer, Integer>runToCompletion("cern.molr.sample.mission.IntDoubler", 21, Integer.class, Integer.class);
        try {
            RunMissionController<Integer> controller = futureController.get();
            FutureRunMissionResult<Integer> futureResult = controller.getResult();
            return futureResult.get();
        } catch (Exception e) {
            //handle
            System.err.println("Operator received an error:");
            throw e;

        }

    }

    public Integer operatorRun3() throws Exception{
        FutureRunMissionController<Integer> futureController =
                mExecService.<Integer, Integer>runToCompletion("cern.molr.sample.mission.Fibonacci",
                        42, Integer.class, Integer.class);

        try {
            RunMissionController<Integer> controller = futureController.get();
            FutureRunMissionCancelResult cancelResult = controller.cancel();
            System.out.println(cancelResult.get().getMessage());
            FutureRunMissionResult<Integer> futureResult = controller.getResult();
            return futureResult.get();
        } catch (Exception e) {
            System.err.println("Operator received an error:");
            throw e;
        }
    }

    
    
}
