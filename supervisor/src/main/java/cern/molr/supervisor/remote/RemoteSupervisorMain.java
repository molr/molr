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
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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

    private final ExecutorService executorService;

    private final ApplicationEventPublisher applicationEventPublisher;


    public RemoteSupervisorMain(AddressGetter addressGetter, SupervisorConfig config, ExecutorService executorService, ApplicationEventPublisher applicationEventPublisher){
        this.addressGetter=addressGetter;
        this.config=config;
        this.executorService = executorService;
        this.applicationEventPublisher = applicationEventPublisher;
        addressGetter.addListener(address -> {
            MolrWebClient client=new MolrWebClient(config.getMolrHost(), config.getMolrPort());
            SupervisorRegisterRequest request=new SupervisorRegisterRequest(address.getHost(),address.getPort(),
                    Arrays.asList(config.getAcceptedMissions()));
            try {
                client.post("/register",SupervisorRegisterRequest.class,request,SupervisorRegisterResponse.class).get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Configuration
    @PropertySource(value="classpath:${supervisor.fileConfig:supervisor.properties}",
            ignoreResourceNotFound=true)
    public static class SupervisorConfigurer {

        private final Environment env;

        public SupervisorConfigurer(Environment env) {
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

            config.setMolrHost(env.getProperty("molr.host","localhost"));
            config.setMolrPort(env.getProperty("molr.port",Integer.class,8000));
            return config;
        }

        @Bean
        public ObjectMapper getMapper() {
            ObjectMapper mapper=new ObjectMapper();
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            return mapper;
        }

        @Bean
        public ExecutorService getExecutorService(){
            return Executors.newFixedThreadPool(10);
        }
    }

    /**
     * A spring event triggered when the supervisor receive a positive response to a regitration request
     * Currently this is only needed in testing
     */
    public static class RegistredEvent extends ApplicationEvent {

        public RegistredEvent(Object source) {
            super(source);
        }
    }

    @PreDestroy
    public void close(){
        executorService.shutdown();
    }

}
