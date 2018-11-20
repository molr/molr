/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;

@Configuration
@ComponentScan
public class ServerConfiguration {

    private final ExecutorService executorService;

    public ServerConfiguration(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @PreDestroy
    public void close() {
        executorService.shutdown();
    }

}
