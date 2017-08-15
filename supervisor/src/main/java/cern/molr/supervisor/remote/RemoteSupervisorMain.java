/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.remote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * Entry point for mole supervisor (which runs a mission using a suitable mole)
 * 
 * @author nachivpn
 */
@SpringBootApplication
public class RemoteSupervisorMain {

    public static void main(String[] args) {
        SpringApplication.run(RemoteSupervisorMain.class, args);
    }
    
}
