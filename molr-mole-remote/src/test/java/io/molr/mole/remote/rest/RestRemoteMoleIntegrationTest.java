package io.molr.mole.remote.rest;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

import java.util.HashMap;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

// for library loggers
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.ListOfStrings;
import io.molr.commons.domain.Mission;
import io.molr.commons.domain.MissionHandle;
import io.molr.commons.domain.MissionParameter;
import io.molr.commons.domain.MissionParameterDescription;
import io.molr.commons.domain.MissionParameterTest;
import io.molr.commons.domain.Placeholder;
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
         assertThat(parameterDescription.parameters()).contains(MissionParameter.required(ParameterTestMissions.CUSTOM).withDefault(ParameterTestMissions.CUSTOM_DEFAULT_VALUE));
         assertThat(parameterDescription.parameters()).contains(MissionParameter.required(ParameterTestMissions.SOME_STRING_ARRAY_PLACEHOLDER));
         
         parameterDescription.parameters().forEach(missionParameter -> {
//            System.out.println(missionParameter);
//            System.out.println(missionParameter.defaultValue().getClass());
//            System.out.println(missionParameter.allowedValues().getClass()); 
         });
             
         
         
         LOGGER.info("Retrieved ParameterDescription: "+parameterDescription);
         Map<String, Object> parameters = new HashMap<>();
         parameters.put("devices", Lists.newArrayList("DEVICE_A, DEVICE_B", "DEVICE_C"));
         parameters.put(ParameterTestMissions.SLEEP_TIME.name(), 500);
         parameters.put(ParameterTestMissions.CUSTOM.name(), new CustomTestParameter(1000, "hello", Lists.newArrayList("hello", "world")));
         parameters.put(ParameterTestMissions.SOME_STRING_ARRAY_PLACEHOLDER.name(), new String[] {"This", "is", "a","test"});
         parameters.put(ParameterTestMissions.GENERIC_STRING_LIST_PLACEHOLDER.name(), Lists.newArrayList("hello", "world"));
         parameters.put(ParameterTestMissions.GENERIC_LONG_LIST_PLACEHOLDER.name(), Lists.newArrayList(1, 2));

         MissionHandle missionHandle = remoteMole.instantiate(parameterMission, parameters).block();
         Thread.sleep(100);
         remoteMole.instructRoot(missionHandle, StrandCommand.RESUME);
         
         remoteMole.outputsFor(missionHandle).subscribe(missionOutput->{
            //only works list is deserialized as ArrayList and Placeholder provides converter
            ListOfStrings listOfStrings = missionOutput.get(Block.builder("1", "hello").build(), Placeholder.aListOfStrings("sequenceOut"));
            LOGGER.info("output: "+listOfStrings);
         });
         
         remoteMole.statesFor(missionHandle).blockLast();
         
     }
     
}

