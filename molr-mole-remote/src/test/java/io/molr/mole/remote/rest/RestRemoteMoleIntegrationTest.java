package io.molr.mole.remote.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
// for library loggers
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ExecutionStrategy;
import io.molr.commons.domain.ListOfStrings;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionParameter;
import io.molr.commons.domain.MissionParameterDescription;
import io.molr.commons.domain.MissionState;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;
import io.molr.commons.domain.StrandCommand;
import io.molr.mole.core.runnable.conf.RunnableLeafMoleConfiguration;
import io.molr.mole.server.rest.MolrMoleRestService;

// for application loggers
//import de.gsi.cs.co.ap.common.gui.elements.logger.AppLogger;

/**
 *
 * @author krepp
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@Import({ParameterTestMissions.class, RunnableLeafMoleConfiguration.class})
@ContextConfiguration(classes = MolrMoleRestService.class)
@EnableAutoConfiguration
@DirtiesContext
@Timeout(value = 60, unit = TimeUnit.SECONDS)
public class RestRemoteMoleIntegrationTest {

     private static final Logger LOGGER = LoggerFactory.getLogger(RestRemoteMoleIntegrationTest.class);

     @Test
     public void test() throws InterruptedException {
         RestRemoteMole remoteMole = new RestRemoteMole("http://localhost:8800");
         Mission parameterMission = new Mission("parameterMission");
         MissionParameterDescription parameterDescription = remoteMole.parameterDescriptionOf(parameterMission).block();

         /*
          * only works since converter from ArrayList to ListOfStrings has been registered
          */
         assertThat(parameterDescription.parameters()).contains(MissionParameter.required(ParameterTestMissions.DEVICES).withDefault(ParameterTestMissions.DEVICES_DEFAULT_VALUE));
         /*
          * the following assertion fails if custom deserialization is not used since default value would be deserialized to a Map 
          */
         assertThat(parameterDescription.parameters()).contains(MissionParameter.required(ParameterTestMissions.CUSTOM)
                 .withDefault(ParameterTestMissions.CUSTOM_DEFAULT_VALUE).withAllowed(ImmutableSet.of(
                         new CustomTestParameter(1000, "hello", Lists.newArrayList("hello", "world")))));
         assertThat(parameterDescription.parameters()).contains(MissionParameter.required(ParameterTestMissions.SOME_STRING_ARRAY_PLACEHOLDER));
         
         parameterDescription.parameters().forEach(missionParameter -> {
//            System.out.println(missionParameter);
//            System.out.println(missionParameter.defaultValue().getClass());
//            System.out.println(missionParameter.allowedValues().getClass()); 
         });
             
         
         
         LOGGER.info("Retrieved ParameterDescription: "+parameterDescription);
         Map<String, Object> parameters = new HashMap<>();
         parameters.put(Placeholders.EXECUTION_STRATEGY.name(), ExecutionStrategy.PROCEED_ON_ERROR);
         parameters.put("devices", Lists.newArrayList("DEVICE_A, DEVICE_B", "DEVICE_C"));
         parameters.put(ParameterTestMissions.SLEEP_TIME.name(), 500);
         parameters.put(ParameterTestMissions.CUSTOM.name(), new CustomTestParameter(1000, "hello", Lists.newArrayList("hello", "world")));
         parameters.put(ParameterTestMissions.SOME_STRING_ARRAY_PLACEHOLDER.name(), new String[] {"This", "is", "a","test"});

         MissionHandle missionHandle = remoteMole.instantiate(parameterMission, parameters).block(Duration.ofSeconds(5));
         Thread.sleep(100);
         remoteMole.instructRoot(missionHandle, StrandCommand.RESUME);
         
         remoteMole.outputsFor(missionHandle).subscribe(missionOutput->{
            //only works as list is deserialized as ArrayList and Placeholder provides converter
            ListOfStrings listOfStrings = missionOutput.get(Block.builder("1", "hello").build(), Placeholder.aListOfStrings("sequenceOut"));
            LOGGER.info("output: "+listOfStrings);
         });
         
         MissionState finalState = remoteMole.statesFor(missionHandle).blockLast(Duration.ofSeconds(5));
         LOGGER.info(finalState.blockIdsToResult().toString());
         
     }
     
}

