package io.molr.mole.server.rest;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import io.molr.mole.server.conf.ObjectMapperConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author krepp
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ContextConfiguration(classes = {MolrMoleRestService.class, ObjectMapperConfig.class})
@EnableAutoConfiguration
public class MolrRestServiceIntegrationTest {

     private static final Logger LOGGER = LoggerFactory.getLogger(MolrRestServiceIntegrationTest.class);
    
    public void test() {
        
    }
    
}

