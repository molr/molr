package org.molr.agency.server.demo;

import org.molr.mole.core.demo.DemoMole;
import org.molr.mole.core.runnable.conf.RunnableLeafMoleConfiguration;
import org.molr.mole.core.runnable.demo.conf.DemoRunnableLeafsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DemoRunnableLeafsConfiguration.class, RunnableLeafMoleConfiguration.class})
public class DemoConfiguration {

    @Bean
    public DemoMole demoSupervisor() {
        return new DemoMole();
    }


}
