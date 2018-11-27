package org.molr.mole.remote.rest;

import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
import org.molr.commons.domain.dto.*;
import org.molr.mole.core.api.Mole;
import org.molr.mole.core.utils.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * This is a rest server that expose all the functionality of the mole it contains
 */


@RestController
public class MolrMoleRestService {

    private final static Logger LOGGER = LoggerFactory.getLogger(MolrMoleRestService.class);

    @Autowired
    private Mole mole;

    @GetMapping(path = "/mission/availableMissions")
    public MissionSetDto availableMissions() {
        return MissionSetDto.from(mole.availableMissions());
    }

    @GetMapping(path = "/mission/{missionName}/representation")
    public MissionRepresentationDto representationOf(@PathVariable("missionName") String missionName) {
            return MissionRepresentationDto.from(mole.representationOf(new Mission(missionName)));
    }

    @GetMapping(path = "/mission/{missionName}/parameterDescription")
    public MissionParameterDescriptionDto parameterDescriptionOf(@PathVariable("missionName") String missionName) {
        return MissionParameterDescriptionDto.from(mole.parameterDescriptionOf(new Mission(missionName)));
    }

    @GetMapping(path = "/instance/{missionHandleId}/states")
    public Flux<MissionStateDto> statesFor(@PathVariable("missionHandleId") String missionHandleId) {
        return mole.statesFor(MissionHandle.ofId(missionHandleId)).map(MissionStateDto::from);
    }

    @GetMapping(path = "/instance/{missionHandleId}/outputs", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<MissionOutputDto> outputsFor(@PathVariable("missionHandleId") String missionHandleId) {
        return mole.outputsFor(MissionHandle.ofId(missionHandleId)).map(MissionOutputDto::from);
    }

    @GetMapping(path = "/instance/{missionHandleId}/representations", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<MissionRepresentationDto> representationsFor(@PathVariable String missionHandleId) {
        return mole.representationsFor(MissionHandle.ofId(missionHandleId)).map(MissionRepresentationDto::from);
    }

    @GetMapping(path = "/test-stream/{count}", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<TestValueDto> testResponse(@PathVariable("count") int count) {
        return Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
                .take(count)
                .map(i ->  new TestValueDto("response number " + i));
    }

    /*
        POST mappings
     */

    @PostMapping(path = "/mission/{missionName}/instantiate/{missionHandleId}")
    public void instantiate(@PathVariable("missionHandleId") String missionHandleId, @PathVariable("missionName") String missionName, @RequestBody Map<String, Object> params) {
        mole.instantiate(MissionHandle.ofId(missionHandleId), new Mission(missionName), params);
    }

    @PostMapping(path = "/instance/{missionHandleId}/{strandId}/instruct/{strandCommand}")
    public void instruct(@PathVariable String missionHandleId, @PathVariable String strandId, @PathVariable String strandCommand) {
        mole.instruct(MissionHandle.ofId(missionHandleId), Strand.ofId(strandId), StrandCommand.valueOf(strandCommand));
    }

    @ExceptionHandler({Exception.class})
    public @ResponseBody ResponseEntity handleException(Exception e){
        return ResponseEntity.badRequest().body(Exceptions.stackTraceFrom(e));
    }

}
