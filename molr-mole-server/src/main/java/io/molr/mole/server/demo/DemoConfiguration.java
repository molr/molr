package io.molr.mole.server.demo;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.molr.mole.core.runnable.conf.RunnableLeafMoleConfiguration;
import io.molr.mole.core.runnable.demo.conf.DemoRunnableLeafsConfiguration;

@Configuration
@Import({ DemoRunnableLeafsConfiguration.class, RunnableLeafMoleConfiguration.class })
public class DemoConfiguration {
    /* Nothing to do here */
}
