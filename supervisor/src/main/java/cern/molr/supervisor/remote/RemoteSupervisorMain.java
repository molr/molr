/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.remote;

import cern.molr.commons.request.supervisor.SupervisorRegisterRequest;
import cern.molr.commons.response.SupervisorRegisterResponse;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.mole.supervisor.MoleSupervisor;
import cern.molr.mole.supervisor.address.AddressGetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Optional;


/**
 * Remote entry point for the {@link MoleSupervisor}
 * When the server is ready, it sends a register request to MolR Server
 * @author nachivpn
 * @author yassine-kr
 */
@SpringBootApplication
public class RemoteSupervisorMain {

    /**
     * In order to specify a supervisor file configuration,
     * the args parameter should contain the element "--supervisor.fileConfig=file_name.properties"
     * If no path specified, the path "supervisor.properties" is used
     * If the used path file does not exist, default configuration values are used
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(RemoteSupervisorMain.class,args);
    }

    private final AddressGetter addressGetter;
    private final SupervisorConfig config;


    public RemoteSupervisorMain(AddressGetter addressGetter,SupervisorConfig config){
        this.addressGetter=addressGetter;
        this.config=config;
        addressGetter.addListener(address -> {
            MolrWebClient client=new MolrWebClient("localhost", 8000);
            SupervisorRegisterRequest request=new SupervisorRegisterRequest(address.getHost(),address.getPort(),
                    Arrays.asList(config.getAcceptedMissions()));
            client.post("/register",SupervisorRegisterRequest.class,request,SupervisorRegisterResponse.class);
        });
    }

    @Configuration
    @PropertySource(value="classpath:${supervisor.fileConfig:supervisor.properties}",
            ignoreResourceNotFound=true)
    public static class ConfigCreator {

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
            config.setAcceptedMissions(Optional.ofNullable(env.getProperty("acceptedMissions"))
                    .map((s)->s.split(",")).orElse(new String[]{}));
            return config;
        }
    }

    @Configuration
    public static class JSONMapperCreator {

        @Bean
        public ObjectMapper getMapper() {
            ObjectMapper mapper=new ObjectMapper();
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            return mapper;
        }
    }

}
