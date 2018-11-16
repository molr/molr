/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.demo;

import cern.molr.client.impl.MissionExecutionServiceImpl;

/**
 * Example implementation of client's usage
 *
 * @author nachivpn
 * @author yassine-kr
 */
public class ClientMain {

    public static void main(String[] args) throws Exception {

        //ExampleOperator operator = new ExampleOperator(new MissionExecutionServiceImpl());
        //operator.parallelExample();

        //ExampleOperator2 operator = new ExampleOperator2(new MissionExecutionServiceImpl());
        //operator.parallelExample();


        new GUIExample(new MissionExecutionServiceImpl());
    }

}
