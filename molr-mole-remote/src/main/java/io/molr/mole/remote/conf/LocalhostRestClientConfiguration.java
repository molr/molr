package io.molr.mole.remote.conf;

import io.molr.mole.core.api.Mole;
import io.molr.mole.remote.rest.RestRemoteMole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalhostRestClientConfiguration {

    @Bean
    public Mole localhostRemoteMole() {
        return new RestRemoteMole("http://localhost:8000");
    }
}
