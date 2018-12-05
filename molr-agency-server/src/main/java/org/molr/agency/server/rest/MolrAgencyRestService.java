package org.molr.agency.server.rest;


import org.molr.commons.api.Agent;
import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
import org.molr.commons.domain.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@RestController
public class MolrAgencyRestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MolrAgencyRestService.class);

    private final Agent agent;

    public MolrAgencyRestService(Agent agent) {
        this.agent = agent;
    }

    /*
        GET mappings
     */

    @GetMapping(path = "/states",  produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<AgencyStateDto> states() {
        return agent.states().map(AgencyStateDto::from);
    }

    @GetMapping(path = "/mission/{missionName}/representation")
    public Mono<MissionRepresentationDto> representationOf(@PathVariable("missionName") String missionName) {
        return agent.representationOf(new Mission(missionName)).map(MissionRepresentationDto::from);
    }

    @GetMapping(path = "/mission/{missionName}/parameterDescription")
    public Mono<MissionParameterDescriptionDto> parameterDescriptionOf(@PathVariable("missionName") String missionName) {
        return agent.parameterDescriptionOf(new Mission(missionName)).map(MissionParameterDescriptionDto::from);
    }

    @GetMapping(path = "/instance/{missionHandle}/states", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<MissionStateDto> statesFor(@PathVariable("missionHandle") String missionHandle) {
        return agent.statesFor(MissionHandle.ofId(missionHandle)).map(MissionStateDto::from);
    }

    @GetMapping(path = "/instance/{missionHandle}/outputs", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<MissionOutputDto> outputsFor(@PathVariable("missionHandle") String missionHandle) {
        return agent.outputsFor(MissionHandle.ofId(missionHandle)).map(MissionOutputDto::from);
    }

    @GetMapping(path = "/instance/{missionHandle}/representations", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<MissionRepresentationDto> representationsFor(@PathVariable("missionHandle") String missionHandle) {
        return agent.representationsFor(MissionHandle.ofId(missionHandle)).map(MissionRepresentationDto::from);
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
        return agent.instantiate(new Mission(missionName), params).map(MissionHandleDto::from);
    }

    @PostMapping(path = "/instance/{missionHandle}/{strandId}/instruct/{commandName}")
    public void instruct(@PathVariable("missionHandle") String missionHandle, @PathVariable("strandId") String strandId, @PathVariable("commandName") String commandName) {
        agent.instruct(MissionHandle.ofId(missionHandle), Strand.ofId(strandId), StrandCommand.valueOf(commandName));
    }

    @PostMapping(path = "/instance/{missionHandle}/instructRoot/{commandName}")
    public void instructRoot(@PathVariable("missionHandle") String missionHandle, @PathVariable("commandName") String commandName) {
        agent.instructRoot(MissionHandle.ofId(missionHandle), StrandCommand.valueOf(commandName));
    }

    @ExceptionHandler({Exception.class})
    public @ResponseBody
    ResponseEntity handleException(Exception e){
        LOGGER.error("Error: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }


}
