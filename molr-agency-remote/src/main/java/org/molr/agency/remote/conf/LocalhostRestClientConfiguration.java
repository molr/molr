package org.molr.agency.remote.conf;

import org.molr.agency.remote.rest.RestRemoteAgency;
import org.molr.commons.api.Agent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalhostRestClientConfiguration {

    @Bean
    public Agent agency() {
        return new RestRemoteAgency("http://localhost:8000");
    }
}
