package io.molr.commons.domain;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class MissionParameterTest {

    @Test
    public void testDefaultValueCanBeNull() {
        MissionParameter<Boolean> parameter = MissionParameter.required(Placeholder.aBoolean("test"));
        Assertions.assertThat(parameter.defaultValue()).isNull();
    }

}