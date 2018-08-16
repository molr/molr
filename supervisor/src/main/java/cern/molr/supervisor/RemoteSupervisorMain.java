/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.molr.supervisor;

import cern.molr.supervisor.api.address.AddressGetter;
import cern.molr.supervisor.api.web.MolrSupervisorToServer;
import cern.molr.supervisor.impl.address.ConfigurationAddressGetter;
import cern.molr.supervisor.impl.web.MolrSupervisorToServerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;


/**
 * Remote entry point for the Supervisor.
 * When the server is ready, it sends a registration request to MolR Server.
 *
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
     */
    public static void main(String[] args) {
        SpringApplication.run(RemoteSupervisorMain.class, args);
    }
}
