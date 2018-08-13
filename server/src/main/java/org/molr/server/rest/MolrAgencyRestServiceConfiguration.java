package org.molr.server.rest;


import org.molr.commons.api.domain.dto.TestValueDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON_VALUE;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

@RestController
public class MolrAgencyRestServiceConfiguration {

//    @Bean
//    public RouterFunction<?> router() {
//        return RouterFunctions.route(RequestPredicates.GET("/test-stream").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), this::testResponse);
//    }

    @RequestMapping(method = RequestMethod.GET, path = "/test-stream/{count}")
    public Flux<TestValueDto> testResponse(@PathVariable("count") int count) {
        return Flux.interval(Duration.of(1, ChronoUnit.SECONDS)).take(count)
                .map(i -> new TestValueDto("Test output " + i));
    }

}
