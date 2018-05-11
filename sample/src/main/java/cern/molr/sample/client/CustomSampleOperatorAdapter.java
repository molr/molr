package cern.molr.sample.client;

import cern.molr.mission.service.custom.adapter.*;

/**
 * Sample implementation and usage of the custom adapter interface to control a mission  execution
 * TODO test this operator after implementing the custom service
 * @author yassine
 */
public class CustomSampleOperatorAdapter {

    /**
     * Usage of MolR by the operator client (the request-response will be done under the hood later)
     */
    MissionExecutionServiceAdapter mExecService;

    public CustomSampleOperatorAdapter(MissionExecutionServiceAdapter mExecService) {
        this.mExecService = mExecService;
    }

    public void operatorRun1() throws Exception{
        FutureRunMissionController<Void> futureController = mExecService.<Void, Void>runToCompletion("cern.molr.sample.mission.RunnableHelloWriter", null, Void.class, Void.class);
        try {
            RunMissionControllerAdapter<Void> controller = futureController.get();
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
            RunMissionControllerAdapter<Integer> controller = futureController.get();
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
            RunMissionControllerAdapter<Integer> controller = futureController.get();
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
