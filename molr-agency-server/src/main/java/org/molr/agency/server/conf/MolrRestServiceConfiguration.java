package org.molr.agency.server.conf;

import org.molr.agency.server.rest.MolrAgencyRestService;
import org.molr.commons.api.Agent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MolrRestServiceConfiguration {

    @Bean
    public MolrAgencyRestService agencyResService(Agent agency) {
        return new MolrAgencyRestService(agency);
    }
}
