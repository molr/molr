package org.molr.mole.server.main;

import org.molr.mole.server.rest.MolrMoleRestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({MolrMoleRestService.class})
public class DemoMoleRestServerMain {

    public static void main(String[] args) {
        SpringApplication.run(DemoMoleRestServerMain.class, args);
    }
}
