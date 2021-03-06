package io.molr.mole.server.rest;

import io.molr.commons.domain.*;
import io.molr.commons.domain.dto.*;
import io.molr.mole.core.api.Mole;
import io.molr.mole.server.conf.ParameterValueDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static io.molr.mole.core.api.MoleWebApi.*;

@RestController
public class MolrMoleRestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MolrMoleRestService.class);

    private final Mole mole;

    public MolrMoleRestService(Mole mole) {
        this.mole = mole;
    }

    /*
        GET mappings
     */

    @GetMapping(path = "/states", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<AgencyStateDto> states() {
        return mole.states().map(AgencyStateDto::from);
    }

    @GetMapping(path = MISSION_REPRESENTATION_PATH)
    public Mono<MissionRepresentationDto> representationOf(@PathVariable(MISSION_NAME) String missionName) {
        return mole.representationOf(new Mission(missionName)).map(MissionRepresentationDto::from);
    }

    @GetMapping(path = MISSION_PARAMETER_DESCRIPTION_PATH)
    public Mono<MissionParameterDescriptionDto> parameterDescriptionOf(@PathVariable(MISSION_NAME) String missionName) {
        return mole.parameterDescriptionOf(new Mission(missionName)).map(MissionParameterDescriptionDto::from);
    }

    @GetMapping(path = INSTANCE_STATES_PATH, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MissionStateDto> statesFor(@PathVariable(MISSION_HANDLE) String missionHandle) {
        return mole.statesFor(MissionHandle.ofId(missionHandle)).map(MissionStateDto::from);
    }

    @GetMapping(path = INSTANCE_OUTPUTS_PATH, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MissionOutputDto> outputsFor(@PathVariable(MISSION_HANDLE) String missionHandle) {
        return mole.outputsFor(MissionHandle.ofId(missionHandle)).map(MissionOutputDto::from);
    }

    @GetMapping(path = INSTANCE_REPRESENTATIONS_PATH, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<MissionRepresentationDto> representationsFor(@PathVariable(MISSION_HANDLE) String missionHandle) {
        return mole.representationsFor(MissionHandle.ofId(missionHandle)).map(MissionRepresentationDto::from);
    }

    @GetMapping(path = "/test-stream/{count}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TestValueDto> testResponse(@PathVariable("count") int count) {
        return Flux.interval(Duration.of(1, ChronoUnit.SECONDS))
                .take(count)
                .map(i -> new TestValueDto("Test output " + i));
    }

    /*
        POST mappings
     */

    @PostMapping(path = INSTANTIATE_MISSION_PATH)
    public Mono<MissionHandleDto> instantiate(@PathVariable(MISSION_NAME) String missionName, @RequestBody String paramsJson) {
        LOGGER.info("json: "+paramsJson);
        
        Mono<Map<String, Object>> parameterMap = mole.parameterDescriptionOf(new Mission(missionName)).flatMap(parameterDescription -> {
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule simpleModule = new SimpleModule();
            simpleModule.addDeserializer(Map.class, ParameterValueDeserializer.with(mapper, parameterDescription));
            mapper.registerModule(simpleModule);
            Map<String, Object> parameterValues = null;
            try {
                parameterValues = mapper.readValue(paramsJson, Map.class);
            } catch (JsonParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (JsonMappingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return Mono.just(parameterValues);
        });
        
        return parameterMap.flatMap(parameterMap1 -> (mole.instantiate(new Mission(missionName), parameterMap1).map(MissionHandleDto::from)));
    }

    @PostMapping(path = INSTANCE_INSTRUCT_MISSION_PATH)
    public void instructMission(@PathVariable(MISSION_HANDLE) final String missionHandle,
            @PathVariable(COMMAND_NAME) final String commandName) {
        mole.instruct(MissionHandle.ofId(missionHandle), MissionCommand.valueOf(commandName));
    }

    @PostMapping(path = INSTANCE_INSTRUCT_PATH)
    public void instruct(@PathVariable(MISSION_HANDLE) String missionHandle, @PathVariable(STRAND_ID) String strandId, @PathVariable(COMMAND_NAME) String commandName) {
        mole.instruct(MissionHandle.ofId(missionHandle), Strand.ofId(strandId), StrandCommand.valueOf(commandName));
    }


    @PostMapping(path = INSTANCE_INSTRUCT_ROOT_PATH)
    public void instructRoot(@PathVariable(MISSION_HANDLE) String missionHandle, @PathVariable(COMMAND_NAME) String commandName) {
        mole.instructRoot(MissionHandle.ofId(missionHandle), StrandCommand.valueOf(commandName));
    }
    
    @PostMapping(path = INSTANCE_INSTRUCT_BLOCK_PATH)
    public void instructBlock(@PathVariable(MISSION_HANDLE) String missionHandle, @PathVariable(BLOCK_ID) String blockId, @PathVariable(COMMAND_NAME) String commandName) {
        mole.instructBlock(MissionHandle.ofId(missionHandle), blockId, BlockCommand.valueOf(commandName));
    }

    @ExceptionHandler({Exception.class})
    public @ResponseBody
    ResponseEntity<?> handleException(Exception e) {
        LOGGER.error("Error: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}
