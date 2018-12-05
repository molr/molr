package org.molr.mole.remote.conf;

import org.molr.mole.remote.rest.RestRemoteMole;
import org.molr.commons.api.Mole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalhostRestClientConfiguration {

    @Bean
    public Mole agency() {
        return new RestRemoteMole("http://localhost:8000");
    }
}
