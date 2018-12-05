package org.molr.agency.remote.rest;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.molr.agency.server.rest.MolrAgencyRestService;
import org.molr.commons.api.Agent;
import org.molr.commons.domain.*;
import org.molr.mole.core.tree.ConcurrentMissionOutputCollector;
import org.molr.mole.core.tree.MissionOutputCollector;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.molr.commons.domain.Placeholder.aString;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ContextConfiguration(classes = MolrAgencyRestService.class)
@EnableAutoConfiguration
public class RestRemoteAgencyTest {

    public static final Block BLOCK2 = Block.idAndText("blockId2", "text");
    public static final Block BLOCK1 = Block.idAndText("blockId1", "text");
    public static final Strand STRAND1 = Strand.ofId("strandId");
    public static final Strand STRAND2 = Strand.ofId("strandId2");
    public static final MissionRepresentation AN_EMPTY_REPRESENTATION = ImmutableMissionRepresentation.empty("anEmpty representation");


    private final String baseUrl = "http://localhost:8000";

    @MockBean
    private Agent agency;

    @Before
    public void setUpAgency() {
        MissionState.Builder builder = MissionState.builder(Result.SUCCESS);
        builder.add(STRAND1, RunState.PAUSED, BLOCK1, Collections.singleton(StrandCommand.RESUME));
        builder.add(STRAND2, RunState.FINISHED, BLOCK2, STRAND1,Collections.emptySet());
        builder.blockResult(BLOCK2, Result.SUCCESS);
        builder.blockRunState(BLOCK2, RunState.FINISHED);
        Set<MissionInstance> missions = new HashSet<>();
        missions.add(new MissionInstance(MissionHandle.ofId("mission1") , new Mission("run a Marathon")));
        missions.add(new MissionInstance(MissionHandle.ofId("mission1") ,new Mission("swim 10km")));
        MissionParameter<?> param = MissionParameter.required(aString("testValue")).withDefault("Test parameter");
        MissionParameterDescription parameterDescription = new MissionParameterDescription(Collections.singleton(param));

        AgencyState agencyState = Mockito.mock(AgencyState.class);
        when(agencyState.activeMissions()).thenReturn(missions);
        when(agency.states()).thenReturn(Flux.just(agencyState));
        when(agency.parameterDescriptionOf(any())).thenReturn(Mono.just(parameterDescription));

        when(agency.representationOf(any(Mission.class))).thenReturn(Mono.just(AN_EMPTY_REPRESENTATION));

        when(agency.representationsFor(any(MissionHandle.class))).thenReturn(Flux.just(AN_EMPTY_REPRESENTATION));

        Flux<MissionState> statesFlux = Flux.fromIterable(Collections.singleton(builder.build()));
        when(agency.statesFor(any(MissionHandle.class))).thenReturn(statesFlux);

        MissionOutputCollector outputCollector = new ConcurrentMissionOutputCollector();
        outputCollector.put(BLOCK1,"example","this is an output example");
        outputCollector.put(BLOCK2,"another example","this is another output example");
        Flux<MissionOutput> outputValue = outputCollector.asStream();
        when(agency.outputsFor(any(MissionHandle.class))).thenReturn(outputValue);
    }

    @org.junit.Test
    public void states() {
        RestRemoteAgency remoteAgency = new RestRemoteAgency(baseUrl);
        Flux<AgencyState> states = remoteAgency.states();
        Assertions.assertThat(states.blockFirst().activeMissions().size()).isEqualTo(2);
    }

    @org.junit.Test
    public void representationOf() {
    }

    @org.junit.Test
    public void parameterDescriptionOf() {
    }

    @org.junit.Test
    public void statesFor() {
    }

    @org.junit.Test
    public void outputsFor() {
    }

    @org.junit.Test
    public void representationsFor() {
    }

    @org.junit.Test
    public void instantiate() {
    }

    @org.junit.Test
    public void instruct() {
    }

    @org.junit.Test
    public void instructRoot() {
    }
}