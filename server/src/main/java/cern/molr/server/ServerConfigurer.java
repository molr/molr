package cern.molr.server;

import cern.molr.commons.conf.ObjectMapperConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@PropertySource(value = "classpath:${server.fileConfig:server.properties}",
        ignoreResourceNotFound = true)
@Import(ObjectMapperConfiguration.class)
public class ServerConfigurer {

    private final Environment env;

    public ServerConfigurer(Environment env) {
        this.env = env;
    }

    @Bean
    public ServerConfig serverConfig() {
        ServerConfig config = new ServerConfig();
        try {
            config.setHeartbeatInterval(env.getProperty("heartbeat.interval", Integer.class, 20));
        } catch (Exception error) {
            config.setHeartbeatInterval(20);
        }
        try {
            config.setHeartbeatTimeOut(env.getProperty("heartbeat.timeOut", Integer.class, 30));
        } catch (Exception error) {
            config.setHeartbeatTimeOut(30);
        }
        try {
            config.setNumMaxTimeOut(env.getProperty("heartbeat.numMaxTimeOut", Integer.class, 1));
        } catch (Exception error) {
            config.setNumMaxTimeOut(1);
        }

        return config;
    }

    /**
     * Executor service needed for running the response to an instantiate request in a thread able to wait using
     * the blocking method of a {@link Mono}
     *
     * @return the executor service
     */
    @Bean
    public ExecutorService getExecutorService() {
        return Executors.newFixedThreadPool(10);
    }

}
