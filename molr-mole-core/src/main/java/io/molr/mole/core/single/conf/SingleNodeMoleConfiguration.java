package io.molr.mole.core.single.conf;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.molr.mole.core.single.SingleNodeMission;
import io.molr.mole.core.single.SingleNodeMole;

@Configuration
public class SingleNodeMoleConfiguration {

    @Autowired
    private Set<SingleNodeMission<?>> singleNodeMissions;

    @Bean
    public SingleNodeMole singleNodeMole() {
        return new SingleNodeMole(singleNodeMissions);
    }
}
