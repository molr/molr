package io.molr.mole.server.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import io.molr.mole.core.conf.LocalSuperMoleConfiguration;
import io.molr.mole.server.conf.SingleMoleRestServiceConfiguration;
import io.molr.mole.server.demo.DemoConfiguration;

@SpringBootApplication
@Import({LocalSuperMoleConfiguration.class, SingleMoleRestServiceConfiguration.class, DemoConfiguration.class})
public class DemoMolrServerMain {

    public static void main(String... args) {
        if (System.getProperty("server.port") == null) {
            System.setProperty("server.port", "8800");
        }
        SpringApplication.run(DemoMolrServerMain.class);
    }

}
