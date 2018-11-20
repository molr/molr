package org.molr.mole.remote.rest;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.molr.commons.domain.ImmutableMissionRepresentation;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionRepresentation;
import org.molr.commons.domain.dto.TestValueDto;
import org.molr.mole.core.api.Mole;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ContextConfiguration(classes = MolrMoleRestService.class)
@EnableAutoConfiguration
public class RestRemoteMoleTest {

    private final String baseUrl = "http://localhost:8800";

    @MockBean
    private Mole mole;

    @Before
    public void setUpMole(){
        Set<Mission> missions = new HashSet<>();
        missions.add(new Mission("run a Marathon"));
        missions.add(new Mission("swim 10km"));
        when(mole.availableMissions()).thenReturn(missions);
        when(mole.representationOf(any(Mission.class))).thenReturn(ImmutableMissionRepresentation.empty("anEmpty representation"));
    }


    @Test
    public void availableMissions() {
        RestRemoteMole remoteMole = new RestRemoteMole(baseUrl);
        Set<Mission> missions = remoteMole.availableMissions();

        System.out.println("Number of available missions : " + missions.size());
    }

    @Test
    public void representationOf() {
        when(mole.representationOf(any(Mission.class))).thenReturn(ImmutableMissionRepresentation.empty("anEmpty representation"));

        RestRemoteMole remoteMole = new RestRemoteMole(baseUrl);
        MissionRepresentation rep = remoteMole.representationOf(new Mission("Linear Mission"));
        System.out.println(" missions : " + rep.toString());
    }

    @Test
    public void parameterDescriptionOf() {
    }

    @Test
    public void instantiate() {
    }
}