package cern.molr.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Configuration
@PropertySource(value = "${mission.fileConfig:classpath:mission.properties}",
        ignoreResourceNotFound = true)
public class MissionConfigurer {

    private final Environment env;

    public MissionConfigurer(Environment env) {
        this.env = env;
    }

    @Bean
    public RegisteredMissions registeredMisions() {
        List<String> missions = Optional.ofNullable(env.getProperty("missions"))
                .map((s) -> s.split(",")).map(Arrays::asList).orElse(Collections.emptyList());
        return new RegisteredMissions(missions);
    }
}
