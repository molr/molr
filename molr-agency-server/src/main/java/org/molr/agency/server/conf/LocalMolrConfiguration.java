package org.molr.agency.server.conf;

import org.molr.agency.core.Agency;
import org.molr.agency.server.local.LocalMoleDelegationAgency;
import org.molr.mole.core.api.Mole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class LocalMolrConfiguration {

    @Autowired
    private Set<Mole> moles;

    @Bean
    public Agency agency() {
        return new LocalMoleDelegationAgency(moles);
    }

}
