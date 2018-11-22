package org.molr.mole.core.single.conf;

import org.molr.mole.core.single.SingleNodeMission;
import org.molr.mole.core.single.SingleNodeMole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class SingleNodeMoleConfiguration {

    @Autowired
    private Set<SingleNodeMission<?>> singleNodeMissions;

    @Bean
    public SingleNodeMole singleNodeMole() {
        return new SingleNodeMole(singleNodeMissions);
    }
}
