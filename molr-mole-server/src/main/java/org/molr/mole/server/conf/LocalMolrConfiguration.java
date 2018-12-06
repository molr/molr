package org.molr.mole.server.conf;

import org.molr.mole.core.api.Mole;
import org.molr.mole.server.local.LocalMoleDelegationAgency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Set;

@Configuration
public class LocalMolrConfiguration {

    @Autowired
    private Set<Mole> moles;

    @Bean
    @Primary
    public Mole agency() {
        return new LocalMoleDelegationAgency(moles);
    }

}
