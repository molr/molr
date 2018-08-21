package org.molr.server.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoConfiguration {

    @Bean
    public DemoSupervisor demoMole() {
        return new DemoSupervisor();
    }

}
