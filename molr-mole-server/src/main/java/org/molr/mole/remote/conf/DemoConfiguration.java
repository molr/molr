package org.molr.mole.remote.conf;

import org.molr.mole.core.api.Mole;
import org.molr.mole.remote.demo.DemoMole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoConfiguration {

    @Bean
    public Mole mole(){
        return new DemoMole();
    }

}
