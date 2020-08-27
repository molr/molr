package io.molr.mole.remote.rest;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.molr.commons.domain.ListOfStrings;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.lang.RunnableLeafsMissionSupport;

/**
 *
 * @author krepp
 */
@Configuration
public class ParameterTestMissions {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterTestMissions.class);
    
    public final static Placeholder<ListOfStrings> DEVICES = Placeholder.aListOfStrings("devices");
    public final static ListOfStrings DEVICES_DEFAULT_VALUE = new ListOfStrings(Lists.newArrayList("a", "b"));
    
    public final static Placeholder<Long> SLEEP_TIME = Placeholder.aLong("sleepTimes");

    public final static CustomTestParameter CUSTOM_DEFAULT_VALUE = new CustomTestParameter(10, "defaultName", new ArrayList<>());
    public final static Placeholder<CustomTestParameter> CUSTOM = Placeholder.of(CustomTestParameter.class, "customParameter");

    
    public final static Placeholder<String[]> SOME_STRING_ARRAY_PLACEHOLDER = Placeholder.aStringArray("someStringArray");
    
    public final static Placeholder<List<String>> GENERIC_STRING_LIST_PLACEHOLDER = Placeholder.aStringListBackedByGenericArrayList("aGenericStringList");
    public final static Placeholder<List<Long>> GENERIC_LONG_LIST_PLACEHOLDER = Placeholder.aLongListBackedByGenericArrayList("aGenericLongList");
    
    public final static Placeholder<UnregisteredCustomType> UNREGISTERED_CUSTOM_TYPE_PLACEHOLDER = Placeholder.of(UnregisteredCustomType.class, "name");
    
    @Bean
    RunnableLeafsMission parameterMission() {
        return new RunnableLeafsMissionSupport() {
            {
                //input
                Placeholder<ListOfStrings> devices = mandatory(DEVICES, DEVICES_DEFAULT_VALUE); 
                Placeholder<Long> longValue = mandatory(SLEEP_TIME, 5000L, Sets.newHashSet(500L, 5000L,3000L,2000L));
                Placeholder<CustomTestParameter> custom = mandatory(CUSTOM, CUSTOM_DEFAULT_VALUE, Sets.newHashSet(new CustomTestParameter(1000, "hello", Lists.newArrayList("hello", "world"))));
                Placeholder<String[]> placeholderSomeStringArray = mandatory(Placeholder.aStringArray("someStringArray"));
                Placeholder<List<String>> placeholderOfGenericStringList = mandatory(GENERIC_STRING_LIST_PLACEHOLDER);
                Placeholder<List<Long>> placeholderOfGenericLongList = mandatory(GENERIC_LONG_LIST_PLACEHOLDER);
                
                /*
                 * If a type is not registered for remote usage. An exception is thrown while parameters are being retrieved
                 * However a remote mole could also validate the parameter description before advertising a mission.
                 */
                // Placeholder<UnregisteredCustomType> placeholderSomeUnregisteredType =
                // mandatory(UNREGISTERED_CUSTOM_TYPE_PLACEHOLDER);
                
                //output
                Placeholder<ListOfStrings> sequenceOut = Placeholder.aListOfStrings("sequenceOut"); 
                
                root("parameterMission").as(rootBranch -> {
                    rootBranch.leaf("simpleTask").run((in, out)-> {
                       LOGGER.info(in.get(devices).toString());
                       LOGGER.info(in.get(longValue).toString());
                       CustomTestParameter customValue = in.get(custom);
                       LOGGER.info(customValue.getName()+ " "+customValue.getValue()+" from custom type");
                       LOGGER.info("customParameterTyoe: "+customValue.getSomeStrings().getClass());
                       String[] stringArray = in.get(placeholderSomeStringArray);
                       LOGGER.info("aStringArray: "+stringArray);
                       List<String> aGenericStringList = in.get(placeholderOfGenericStringList);
                       LOGGER.info("stringList: "+aGenericStringList);
                       List<Long> aGenericLongList = in.get(placeholderOfGenericLongList);
                       LOGGER.info("longList "+aGenericLongList);
//                       LOGGER.info(in.get(strings).toString());
                       out.emit(sequenceOut, new ListOfStrings(Lists.newArrayList("out1", "out2", "...")));
                    });
                });
            }          
        }.build();
    }

}

