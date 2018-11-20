package org.molr.mole.remote.main;

import org.molr.mole.remote.conf.DemoConfiguration;
import org.molr.mole.remote.rest.MolrMoleRestService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({DemoConfiguration.class, MolrMoleRestService.class})
public class DemoMoleRestServerMain {

    public static void main(String[] args){
        SpringApplication.run(DemoMoleRestServerMain.class, args);
    }
}
