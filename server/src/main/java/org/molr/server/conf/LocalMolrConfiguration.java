package org.molr.server.conf;

import org.molr.commons.api.domain.AtomicIncrementMissionHandleFactory;
import org.molr.commons.api.domain.MissionHandleFactory;
import org.molr.commons.api.service.Agency;
import org.molr.mole.api.Supervisor;
import org.molr.server.local.LocalMoleDelegationAgency;
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
