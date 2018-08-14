/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.server;

import cern.molr.commons.web.SerializationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

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
    @PropertySource(value = "classpath:${server.fileConfig:server.properties}",
            ignoreResourceNotFound = true)
    public static class ServerConfigurer {

        private final Environment env;

        public ServerConfigurer(Environment env) {
            this.env = env;
        }

        @Bean
        public ServerConfig getSupervisorConfig() {
            ServerConfig config = new ServerConfig();
            try {
                config.setHeartbeatInterval(env.getProperty("heartbeat.interval", Integer.class, 20));
            } catch (Exception error) {
                config.setHeartbeatInterval(20);
            }
            try {
                config.setHeartbeatTimeOut(env.getProperty("heartbeat.timeOut", Integer.class, 30));
            } catch (Exception error) {
                config.setHeartbeatTimeOut(30);
            }
            try {
                config.setNumMaxTimeOut(env.getProperty("heartbeat.numMaxTimeOut", Integer.class, 1));
            } catch (Exception error) {
                config.setNumMaxTimeOut(1);
            }

            return config;
        }


        @Bean
        public ObjectMapper getMapper() {
            return SerializationUtils.getMapper();
        }

        /**
         * Executor service needed for running the response to an instantiate request in a thread able to wait using
         * the blocking method of a {@link Mono}
         *
         * @return the executor service
         */
        @Bean
        public ExecutorService getExecutorService() {
            return Executors.newFixedThreadPool(10);
        }

    }

}
