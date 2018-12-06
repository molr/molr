package org.molr.mole.server.main;

import org.molr.mole.core.conf.LocalSuperMoleConfiguration;
import org.molr.mole.server.conf.SingleMoleRestServiceConfiguration;
import org.molr.mole.server.demo.DemoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({LocalSuperMoleConfiguration.class, SingleMoleRestServiceConfiguration.class, DemoConfiguration.class})
public class DemoMolrServerMain {

    public static void main(String... args) {
        if (System.getProperty("server.port") == null) {
            System.setProperty("server.port", "8000");
        }
        SpringApplication.run(DemoMolrServerMain.class);
    }

}
