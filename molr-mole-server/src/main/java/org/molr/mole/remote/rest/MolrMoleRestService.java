package org.molr.mole.remote.rest;

import org.molr.commons.domain.Mission;
import org.molr.commons.domain.MissionHandle;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;
import org.molr.commons.domain.dto.*;
import org.molr.mole.core.api.Mole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON_VALUE;

@RestController
public class MolrMoleRestService {

    @Autowired
    Mole mole;


    @GetMapping(path = "mission/availableMissions")
    public MissionSetDto availableMissions() {
        Set<Mission> missions = mole.availableMissions();
        return MissionSetDto.from(missions);
    }

    @GetMapping(path = "mission/{missionName}/representation")
    public MissionRepresentationDto representationOf(@PathVariable("missionName") String missionName) {
        return MissionRepresentationDto.from(mole.representationOf(new Mission(missionName)));
    }

    @GetMapping(path = "mission/{missionName}/parameterDescription")
    public MissionParameterDescriptionDto parameterDescriptionOf(@PathVariable("MissionName") String missionName) {
        return MissionParameterDescriptionDto.from(mole.parameterDescriptionOf(new Mission(missionName)));
    }

    @PostMapping(path = "mission/{missionMame}/instantiate/{missionHandleId}")
    public void instantiate(@PathVariable("missionHandleId") String missionHandleId, @PathVariable("missionName") String missionName, @RequestBody Map<String, Object> params) {
        mole.instantiate(MissionHandle.ofId(missionHandleId), new Mission(missionName), params);
    }

    @GetMapping(path = "mission/status/{missionHandleId}")
    public Flux<MissionStateDto> statesFor(@PathVariable String missionHandleId) {
        return mole.statesFor(MissionHandle.ofId(missionHandleId)).map(MissionStateDto::from);
    }

    @PostMapping(path = "mission/instruct/{missionHandleId}/{strandId}/{standCommand}")
    public void instruct(@PathVariable String missionHandleId, @PathVariable String strandId, @PathVariable String command) {
        mole.instruct(MissionHandle.ofId(missionHandleId), Strand.ofId(strandId), StrandCommand.valueOf(command));
    }

    @GetMapping(path = "mission/outputsFor/{missionHandleId}")
    public Flux<MissionOutputDto> outputsFor(@PathVariable String missionHandleId) {
        return mole.outputsFor(MissionHandle.ofId(missionHandleId)).map(MissionOutputDto::from);
    }

    @GetMapping(path = "mission/representationFor/{missionHandleId}")
    public Flux<MissionRepresentationDto> representationsFor(@PathVariable String missionHandleId) {
        return mole.representationsFor(MissionHandle.ofId(missionHandleId)).map(MissionRepresentationDto::from);
    }

    @GetMapping(path = "/test-stream/{count}", produces = APPLICATION_STREAM_JSON_VALUE)
    public Flux<TestValueDto> testResponse(@PathVariable("count") int count) {
        return Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
                .take(count)
                .map(i ->  new TestValueDto("response number " + i));
    }

}
