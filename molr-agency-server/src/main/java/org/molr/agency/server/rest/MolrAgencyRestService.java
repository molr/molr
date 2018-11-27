package org.molr.agency.server.rest;


import org.molr.agency.core.Agency;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
import org.molr.commons.domain.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
public class MolrAgencyRestService {

    /**
     * The agency to which the calls shall be delegated
     */
    @Autowired
    private Agency agency;

    /*
        GET mappings
     */

    @GetMapping(path = "/states",  produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<AgencyStateDto> states() {
        return agency.states().map(AgencyStateDto::from);
    }

    @GetMapping(path = "/mission/{missionName}/representation")
    public Mono<MissionRepresentationDto> representationOf(@PathVariable("missionName") String missionName) {
        return agency.representationOf(new Mission(missionName)).map(MissionRepresentationDto::from);
    }

    @GetMapping(path = "/mission/{missionName}/parameterDescription")
    public Mono<MissionParameterDescriptionDto> parameterDescriptionOf(@PathVariable("missionName") String missionName) {
        return agency.parameterDescriptionOf(new Mission(missionName)).map(MissionParameterDescriptionDto::from);
    }

    @GetMapping(path = "/instance/{missionHandle}/states", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<MissionStateDto> statesFor(@PathVariable("missionHandle") String missionHandle) {
        return agency.statesFor(MissionHandle.ofId(missionHandle)).map(MissionStateDto::from);
    }

    @GetMapping(path = "/instance/{missionHandle}/outputs", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<MissionOutputDto> outputsFor(@PathVariable("missionHandle") String missionHandle) {
        return agency.outputsFor(MissionHandle.ofId(missionHandle)).map(MissionOutputDto::from);
    }

    @GetMapping(path = "/instance/{missionHandle}/representations", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<MissionRepresentationDto> representationsFor(@PathVariable("missionHandle") String missionHandle) {
        return agency.representationsFor(MissionHandle.ofId(missionHandle)).map(MissionRepresentationDto::from);
    }

    @GetMapping(path = "/test-stream/{count}", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<TestValueDto> testResponse(@PathVariable("count") int count) {
        return Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
                .take(count)
                .map(i -> new TestValueDto("Test output " + i));
    }

    /*
        POST mappings
     */

    @PostMapping(path = "/mission/{missionName}/instantiate")
    public Mono<MissionHandleDto> instantiate(@PathVariable("missionName") String missionName, @RequestBody Map<String, Object> params) {
        return agency.instantiate(new Mission(missionName), params).map(MissionHandleDto::from);
    }

    @PostMapping(path = "/instance/{missionHandle}/{strandId}/instruct/{commandName}")
    public void instruct(@PathVariable("missionHandle") String missionHandle, @PathVariable("strandId") String strandId, @PathVariable("commandName") String commandName) {
        agency.instruct(MissionHandle.ofId(missionHandle), Strand.ofId(strandId), StrandCommand.valueOf(commandName));
    }

}
