package io.molr.mole.core.runnable.conf;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.RunnableLeafsMole;

@Configuration
public class RunnableLeafMoleConfiguration {

    @Autowired
    private Set<RunnableLeafsMission> missions;

    @Bean
    public RunnableLeafsMole runnableLeafsMole() {
        return new RunnableLeafsMole(missions);
    }

}
