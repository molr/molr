package org.molr.mole.remote;

import org.molr.commons.domain.dto.TestValueDto;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

public class MoleRestServerTest {


    public static void main(String[] args) throws IOException {
       WebClient client = WebClient.create("http://localhost:8800");
        client.get()
                .uri("/test-stream/4")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .subscribe(res -> res.bodyToFlux(TestValueDto.class).subscribe(v -> System.out.println(v)));
        System.in.read();
    }

}
