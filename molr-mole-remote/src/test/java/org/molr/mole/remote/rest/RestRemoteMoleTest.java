package org.molr.mole.remote.rest;

import org.junit.Test;
import org.molr.commons.domain.Mission;

import java.util.Set;

class RestRemoteMoleTest {

    private final String baseUrl = "http://localhost:8800";

    @Test
    void availableMissions() {
        RestRemoteMole remoteMole = new RestRemoteMole(baseUrl);
        Set<Mission> missions = remoteMole.availableMissions();
        System.out.println("Number of available missions : " + missions.size());
    }

    @Test
    void representationOf() {
    }

    @Test
    void parameterDescriptionOf() {
    }

    @Test
    void instantiate() {
    }
}