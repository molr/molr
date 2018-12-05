package org.molr.agency.server.conf;

import org.molr.agency.server.local.LocalMoleDelegationAgency;
import org.molr.commons.api.Agent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class LocalMolrConfiguration {

    @Autowired
    private Set<Agent> moles;

    @Bean
    public Agent agency() {
        return new LocalMoleDelegationAgency(moles);
    }

}
