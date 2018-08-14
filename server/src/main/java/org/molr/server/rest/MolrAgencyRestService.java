package org.molr.server.rest;


import org.molr.commons.api.domain.*;
import org.molr.commons.api.domain.dto.AgencyStateDto;
import org.molr.commons.api.domain.dto.MissionDto;
import org.molr.commons.api.domain.dto.MissionHandleDto;
import org.molr.commons.api.domain.dto.TestValueDto;
import org.molr.server.api.Agency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON_VALUE;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

@RestController
public class MolrAgencyRestService {

    /**
     * The agency to which the calls shall be delegated
     */
    @Autowired
    private Agency agency;

    @GetMapping(path = "/executable-missions")
    public Flux<MissionDto> executableMissions() {
        return agency.executableMissions().map(MissionDto::from);
    }

    @GetMapping(path = "/{missionName}/instantiate")
    public Mono<MissionHandleDto> instantiate(@PathVariable("missionName") String missionName) {
        /* TODO: Implement real parameters */
        Map<String, Object> params = Collections.emptyMap();
        return agency.instantiate(new Mission(missionName), params).map(MissionHandleDto::from);
    }

    public Mono<MissionRepresentation> representationOf(Mission mission) {
        return agency.representationOf(mission);
    }

    @GetMapping(path = "/states")
    public Flux<AgencyStateDto> states() {
        return agency.states().map(AgencyStateDto::from);
    }

    public Flux<MissionState> statesFor(MissionHandle handle) {
        return agency.statesFor(handle);
    }

    public void instruct(MissionHandle handle, MissionCommand command) {
        agency.instruct(handle, command);
    }

    @GetMapping(path = "/test-stream/{count}")
    public Flux<TestValueDto> testResponse(@PathVariable("count") int count) {
        return Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
                .take(count)
                .map(i -> new TestValueDto("Test output " + i));
    }
}
