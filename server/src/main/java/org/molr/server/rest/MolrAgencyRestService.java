package org.molr.server.rest;


import org.molr.commons.api.domain.*;
import org.molr.commons.api.domain.dto.*;
import org.molr.commons.api.service.Agency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

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

    @GetMapping(path = "/instance/{missionHandle}/instruct/{commandName}")
    public void instruct(@PathVariable("missionHandle") String missionHandle, @PathVariable("commandName") String commandName) {
        agency.instruct(MissionHandle.ofId(missionHandle), MissionCommand.valueOf(commandName));
    }

    @GetMapping(path = "/test-stream/{count}")
    public Flux<TestValueDto> testResponse(@PathVariable("count") int count) {
        return Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
                .take(count)
                .map(i -> new TestValueDto("Test output " + i));
    }
}
