package io.molr.commons.domain;

import static org.assertj.core.api.Assertions.*;
import org.junit.Test;

import com.google.common.collect.Sets;

import io.molr.commons.domain.dto.MissionParameterDto;

public class MissionParameterTest {

    @Test
    public void testDefaultValueCanBeNull() {
        MissionParameter<Boolean> parameter = MissionParameter.required(Placeholder.aBoolean("test"));
        assertThat(parameter.defaultValue()).isNull();
    }
    
    @Test
    public void withAllowed_whenAllowedValuesGiven_allowedValuesAreConvertedBackAndForthToAndFromDto() {
        MissionParameter<Double> parameter = MissionParameter.required(Placeholder.aDouble("number"))
                .withAllowed(Sets.newHashSet(5d, 3d)).withDefault(5d);
        assertThat(parameter.allowedValues()).containsExactlyInAnyOrder(5d,3d);
        
        MissionParameterDto<Double> parameterDto = MissionParameterDto.from(parameter);
        assertThat(parameterDto.allowedValues).containsExactlyInAnyOrder(3d, 5d);
        
        MissionParameter<Double> parameterFromDto = parameterDto.toMissionParameter();
        assertThat(parameterFromDto.allowedValues()).containsExactlyInAnyOrder(3d, 5d);
    }
    
    @Test
    public void withAllowed_whenNoneAllowedValuesGiven_emptyAllowedValuesAreConvertedBackAndForthToAndFromDto() {
        MissionParameter<Double> parameter = MissionParameter.required(Placeholder.aDouble("number"));
        assertThat(parameter.allowedValues()).containsExactlyInAnyOrder();
        
        MissionParameterDto<Double> parameterDto = MissionParameterDto.from(parameter);
        assertThat(parameterDto.allowedValues).containsExactlyInAnyOrder();
        
        MissionParameter<Double> parameterFromDto = parameterDto.toMissionParameter();
        assertThat(parameterFromDto.allowedValues()).containsExactlyInAnyOrder();
    }
    

}