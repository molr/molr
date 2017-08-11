/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.operator;

import cern.molr.sample.client.SampleOperator;
import cern.molr.serviceimpl.MissionExecutionServiceImpl;

public class OperatorSampleMain {

    public static void main(String[] args) throws Exception {
        SampleOperator operator = new SampleOperator(new MissionExecutionServiceImpl());
        System.out.println("The meaning of life is " + operator.operatorRun2());
    }
    
}
