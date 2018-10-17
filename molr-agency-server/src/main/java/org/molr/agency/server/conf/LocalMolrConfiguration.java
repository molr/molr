package org.molr.agency.server.conf;

import org.molr.agency.core.Agency;
import org.molr.agency.server.local.LocalMoleDelegationAgency;
import org.molr.commons.domain.AtomicIncrementMissionHandleFactory;
import org.molr.commons.domain.MissionHandleFactory;
import org.molr.mole.core.api.Supervisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class LocalMolrConfiguration {

    @Autowired
    private Set<Supervisor> supervisors;

    @Bean
    public MissionHandleFactory missionHandleFactory() {
        return new AtomicIncrementMissionHandleFactory();
    }

    @Bean
    public Agency agency(MissionHandleFactory missionHandleFactory) {
        return new LocalMoleDelegationAgency(missionHandleFactory, supervisors);
    }

}
