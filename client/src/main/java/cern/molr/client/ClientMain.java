/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.client;

import cern.molr.client.serviceimpl.MissionExecutionServiceImpl;
import cern.molr.client.serviceimpl.adapter.MissionExecutionServiceAdapterImpl;
import cern.molr.sample.client.SampleOperator;
import cern.molr.sample.client.SampleOperatorAdapter;
import reactor.core.publisher.Mono;

/**
 * Example implementation of client's usage & functionalities using samples
 * 
 * @author nachivpn
 */
public class ClientMain {

    public static void main(String[] args) throws Exception {
        //SampleOperator operator = new SampleOperator(new MissionExecutionServiceImpl());
        //System.out.println("The meaning of life is " + operator.operatorRun2());
        //System.out.println("The meaning of life is " + operator.operatorRun3());

        SampleOperatorAdapter operatorAdapter = new SampleOperatorAdapter(new MissionExecutionServiceAdapterImpl(new MissionExecutionServiceImpl()));
        System.out.println("The meaning of life is " + operatorAdapter.operatorRun3());
    }
    
}
