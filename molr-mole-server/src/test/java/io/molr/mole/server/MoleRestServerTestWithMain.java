package io.molr.mole.server;

import io.molr.commons.domain.dto.TestValueDto;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

public class MoleRestServerTestWithMain {


    public static void main(String[] args) throws IOException {

        WebClient client = WebClient.create("http://localhost:8800");
        TestValueDto returnedObject = client.get()
                .uri("/tests/testOK")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .flatMap(response -> response.bodyToMono(TestValueDto.class)).block();
        System.out.println("Returned object contains : " + returnedObject.text);
        System.in.read();
    }

}
