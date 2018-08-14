package org.molr.server.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class DemoConfiguration {

    @Bean
    public DemoMole demoMole() {
        return new DemoMole();
    }

}
