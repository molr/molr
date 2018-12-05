package org.molr.mole.server.main;

import org.molr.mole.server.conf.LocalMolrConfiguration;
import org.molr.mole.server.demo.DemoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({LocalMolrConfiguration.class, DemoConfiguration.class})
public class DemoMolrAgencyServerMain {

    public static void main(String... args) {
        if (System.getProperty("server.port") == null) {
            System.setProperty("server.port", "8000");
        }
        SpringApplication.run(DemoMolrAgencyServerMain.class);
    }

}
