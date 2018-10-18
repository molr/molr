package org.molr.agency.server.rest;


import org.molr.agency.core.Agency;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.StrandCommand;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.dto.AgencyStateDto;
import org.molr.commons.domain.dto.MissionHandleDto;
import org.molr.commons.domain.dto.MissionRepresentationDto;
import org.molr.commons.domain.dto.MissionStateDto;
import org.molr.commons.domain.dto.TestValueDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

@RestController
public class MolrAgencyRestService {

    /**
     * The agency to which the calls shall be delegated
     */
    @Autowired
    private Agency agency;

    @GetMapping(path = "/mission/{missionName}/instantiate")
    public Mono<MissionHandleDto> instantiate(@PathVariable("missionName") String missionName) {
        /* TODO: Implement real parameters */
        Map<String, Object> params = Collections.emptyMap();
        return agency.instantiate(new Mission(missionName), params).map(MissionHandleDto::from);
    }

    @GetMapping(path = "/mission/{missionName}/representation")
    public Mono<MissionRepresentationDto> representationOf(@PathVariable("missionName") String missionName) {
        return agency.representationOf(new Mission(missionName)).map(MissionRepresentationDto::from);
    }

    @GetMapping(path = "/states")
    public Flux<AgencyStateDto> states() {
        return agency.states().map(AgencyStateDto::from);
    }

    @GetMapping(path = "/instance/{missionHandle}/states")
    public Flux<MissionStateDto> statesFor(@PathVariable("missionHandle") String missionHandle) {
        return agency.statesFor(MissionHandle.ofId(missionHandle)).map(MissionStateDto::from);
    }

    @GetMapping(path = "/instance/{missionHandle}/{strandId}/instruct/{commandName}")
    public void instruct(@PathVariable("missionHandle") String missionHandle, @PathVariable("strandId") String strandId, @PathVariable("commandName") String commandName) {
        agency.instruct(MissionHandle.ofId(missionHandle), Strand.ofId(strandId), StrandCommand.valueOf(commandName));
    }

    @GetMapping(path = "/test-stream/{count}", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<TestValueDto> testResponse(@PathVariable("count") int count) {
        return Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
                .take(count)
                .map(i -> new TestValueDto("Test output " + i));
    }



}
