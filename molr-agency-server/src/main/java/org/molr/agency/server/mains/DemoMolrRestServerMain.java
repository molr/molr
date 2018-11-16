package org.molr.agency.server.mains;

import org.molr.agency.server.conf.LocalMolrConfiguration;
import org.molr.agency.server.demo.DemoConfiguration;
import org.molr.agency.server.rest.MolrAgencyRestService;
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
