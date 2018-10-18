package org.molr.agency.server.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoConfiguration {

    @Bean
    public DemoMole demoSupervisor() {
        return new DemoMole();
    }



}
