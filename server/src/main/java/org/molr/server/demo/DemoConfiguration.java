package org.molr.server.demo;

import cern.molr.sample.mission.SequenceMissionExample;
import cern.molr.sample.mole.SequenceMission;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Set;

@Configuration
public class DemoConfiguration {

    @Bean
    public DemoSupervisor demoSupervisor() {
        return new DemoSupervisor();
    }



}
