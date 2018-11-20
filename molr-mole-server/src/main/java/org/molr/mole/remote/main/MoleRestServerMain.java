package org.molr.mole.remote.main;

import org.molr.mole.remote.conf.DemoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(DemoConfiguration.class)
public class MoleRestServerMain {

    public static void main(String[] args){
        SpringApplication.run(MoleRestServerMain.class, args);
    }
}
