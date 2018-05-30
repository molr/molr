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

        SampleOperator operator=new SampleOperator(new MissionExecutionServiceImpl());
        operator.parallelExample();
    }
    
}
