package org.molr.mole.server.conf;

import org.molr.mole.core.api.Mole;
import org.molr.mole.server.rest.MolrMoleRestService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MolrRestServiceConfiguration {

    @Bean
    public MolrMoleRestService agencyResService(Mole agency) {
        return new MolrMoleRestService(agency);
    }
}
