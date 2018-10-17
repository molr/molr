package org.molr.server.mains;

import org.molr.server.conf.LocalMolrConfiguration;
import org.molr.server.demo.DemoConfiguration;
import org.molr.server.rest.MolrAgencyRestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({MolrAgencyRestService.class, LocalMolrConfiguration.class, DemoConfiguration.class})

public class DemoMolrRestServerMain {

    public static void main(String... args) {
        SpringApplication.run(DemoMolrRestServerMain.class);
    }

}
