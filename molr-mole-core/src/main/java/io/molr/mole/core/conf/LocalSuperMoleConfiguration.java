package io.molr.mole.core.conf;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.molr.mole.core.api.Mole;
import io.molr.mole.core.local.LocalSuperMole;

@Configuration
public class LocalSuperMoleConfiguration {

    @Autowired
    private Set<Mole> moles;

    @Bean
    @Primary
    public LocalSuperMole superMole() {
        return new LocalSuperMole(moles);
    }

}
