/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class ServerMain {

    private final ExecutorService executorService;

    public ServerMain(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerMain.class, args);
    }

    @PreDestroy
    public void close() {
        executorService.shutdown();
    }

    @Configuration
    public static class ServerConfigurer {

        @Bean
        public ObjectMapper getMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            return mapper;
        }

        @Bean
        public ExecutorService getExecutorService() {
            return Executors.newFixedThreadPool(10);
        }

    }

}
