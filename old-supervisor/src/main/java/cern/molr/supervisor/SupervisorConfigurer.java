package cern.molr.supervisor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.Optional;

@Configuration
@PropertySource(value = "${supervisor.fileConfig:classpath:supervisor.properties}",
        ignoreResourceNotFound = true)
public class SupervisorConfigurer {

    private final Environment env;

    public SupervisorConfigurer(Environment env) {
        this.env = env;
    }

    @Bean
    public SupervisorConfig getSupervisorConfig() {
        SupervisorConfig config = new SupervisorConfig();
        try {
            config.setMaxMissions(env.getProperty("maxMissions", Integer.class, 1));
        } catch (Exception error) {
            config.setMaxMissions(1);
        }
        //noinspection ConstantConditions
        config.setAcceptedMissions(Optional.ofNullable(env.getProperty("acceptedMissions"))
                .map((s) -> s.split(",")).orElse(new String[]{}));

        config.setMolrHost(env.getProperty("molr.host", "http://localhost"));
        config.setMolrPort(env.getProperty("molr.port", Integer.class, 8000));

        config.setSupervisorHost(env.getProperty("supervisor.host"));
        try {
            config.setSupervisorPort(env.getProperty("supervisor.port", Integer.class, -1));
        } catch (Exception error) {
            config.setSupervisorPort(-1);
        }

        return config;
    }

}
