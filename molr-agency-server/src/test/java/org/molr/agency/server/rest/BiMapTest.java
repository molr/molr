package org.molr.agency.server.rest;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

public class BiMapTest {

    @Test
    public void inverse() {
        ImmutableMap<String, String> map = ImmutableMap.of("A", "1", "B", "1");
        System.out.println(map);

        ImmutableBiMap<String, String> inverse = ImmutableBiMap.copyOf(map
        ).inverse();

        System.out.println(inverse);
    }
}
