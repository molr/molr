/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client;

import cern.molr.client.serviceimpl.MissionExecutionServiceImpl;

/**
 * Example implementation of client's usage & functionalities using samples
 * 
 * @author nachivpn
 * @author yassine-kr
 */
public class ClientMain {

    public static void main(String[] args) throws Exception {
        /*
        CustomSampleOperator operator = new CustomSampleOperator(new CustomMissionExecutionServiceImpl());
        System.out.println("The meaning of life is " + operator.operatorRun2());
        System.out.println("The meaning of life is " + operator.operatorRun3());

        CustomSampleOperatorAdapter operatorAdapter = new CustomSampleOperatorAdapter(
                new MissionExecutionServiceAdapterImpl(new CustomMissionExecutionServiceImpl()));
        System.out.println("The meaning of life is " + operatorAdapter.operatorRun3());
        */

        SampleOperator operator=new SampleOperator(new MissionExecutionServiceImpl());
        operator.parallelExample();
    }
    
}
