/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.remote;

import cern.molr.commons.response.SupervisorRegisterResponse;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.request.supervisor.SupervisorRegisterRequest;
import cern.molr.mole.supervisor.MoleSupervisor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * Remote entry point for the {@link MoleSupervisor}
 * When the server is ready, it sends a register request to MolR Server
 * TODO send the real host address of the server instead of 'localhost'
 * @author nachivpn
 * @author yassine
 */
@SpringBootApplication
public class RemoteSupervisorMain {

    public static void main(String[] args) {
        List<String> l=Arrays.asList(args);
        List<String> a=new LinkedList<>();
        a.addAll(l);
        //a.add("--supervisor.fileConfig=supervisor.properties");
        SpringApplication.run(RemoteSupervisorMain.class,a.toArray(new String[]{}));
    }

    @Component
    public static class Listener implements ApplicationListener<WebServerInitializedEvent> {

        private SupervisorConfig config;

        public Listener(SupervisorConfig config) {
            this.config = config;
        }

        @Override
        public void onApplicationEvent(final WebServerInitializedEvent event) {

            int port = event.getWebServer().getPort();

            MolrWebClient client=new MolrWebClient("localhost", 8000);
            SupervisorRegisterRequest request=new SupervisorRegisterRequest("localhost",port, Arrays.asList(config.getAcceptedMissions()));
            client.post("/register",SupervisorRegisterRequest.class,request,SupervisorRegisterResponse.class);


        }
    }

    @Configuration
    @PropertySource(value="classpath:${supervisor.fileConfig:supervisor.properties}",
            ignoreResourceNotFound=true)
    public class ConfigCreator {

        private final Environment env;

        public ConfigCreator(Environment env) {
            this.env=env;
        }

        @Bean
        public SupervisorConfig getSupervisorConfig() {
            SupervisorConfig config = new SupervisorConfig();
            try {
                config.setMaxMissions(env.getProperty("maxMissions",Integer.class,1));
            }catch(Exception e){
                config.setMaxMissions(1);
            }
            //noinspection ConstantConditions
            config.setAcceptedMissions(Optional.ofNullable(env.getProperty("acceptedMissions")).map((s)->s.split(",")).orElse(new String[]{}));
            return config;
        }
    }

    @Configuration
    public class JSONMapperCreator {

        @Bean
        public ObjectMapper getMapper() {
            ObjectMapper mapper=new ObjectMapper();
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            return mapper;
        }
    }

}
