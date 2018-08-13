package org.molr.server.mains;

import org.molr.server.rest.MolrAgencyRestServiceConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(MolrAgencyRestServiceConfiguration.class)
public class DemoMolrRestServerMain {

    public static void main(String... args) {
        SpringApplication.run(DemoMolrRestServerMain.class);
    }

}
