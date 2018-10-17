package org.molr.client.conf;

import org.molr.client.rest.RestRemoteAgency;
import org.molr.commons.api.service.Agency;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalhostRestClientConfiguration {

    @Bean
    public Agency agency() {
        return new RestRemoteAgency("http://localhost:8000");
    }
}
