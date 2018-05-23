/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor.remote;

import cern.molr.commons.response.SupervisorRegisterResponse;
import cern.molr.commons.web.MolrWebClient;
import cern.molr.commons.request.supervisor.SupervisorRegisterRequest;
import cern.molr.mole.supervisor.MoleSupervisor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;


/**
 * Remote entry point for the {@link MoleSupervisor}
 * When the server is ready, it sends a register request to MolR Server
 * TODO send the real host address of the server instead of 'localhost'
 * @author nachivpn
 * @author yassine-kr
 */
@SpringBootApplication
public class RemoteSupervisorMain {

    public static void main(String[] args) {
        SpringApplication.run(RemoteSupervisorMain.class, args);
    }

    @Component
    public static class Listener implements ApplicationListener<WebServerInitializedEvent> {

        @Override
        public void onApplicationEvent(final WebServerInitializedEvent event) {


            int port = event.getWebServer().getPort();

            MolrWebClient client=new MolrWebClient("localhost", 8000);
            SupervisorRegisterRequest request=new SupervisorRegisterRequest("localhost",port, Arrays.asList(
                    "cern.molr.sample.mission.RunnableHelloWriter",
                    "cern.molr.sample.mission.IntDoubler",
                    "cern.molr.sample.mission.Fibonacci"));
            client.post("/register",SupervisorRegisterRequest.class,request,SupervisorRegisterResponse.class);


        }
    }
}
