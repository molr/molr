package io.molr.mole.server.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.molr.mole.core.api.Mole;
import io.molr.mole.server.rest.MolrMoleRestService;

/**
 * Configuration that creates a REST service with the ONLY {@link Mole} in the context
 */
@Configuration
public class SingleMoleRestServiceConfiguration {

    @Bean
    public static MolrMoleRestService agencyResService(Mole mole) {
        return new MolrMoleRestService(mole);
    }
}
