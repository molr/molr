package io.molr.mole.server.demo;

import io.molr.mole.core.runnable.conf.RunnableLeafMoleConfiguration;
import io.molr.mole.core.runnable.demo.conf.DemoRunnableLeafsConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({DemoRunnableLeafsConfiguration.class, RunnableLeafMoleConfiguration.class})
public class DemoConfiguration {

}
