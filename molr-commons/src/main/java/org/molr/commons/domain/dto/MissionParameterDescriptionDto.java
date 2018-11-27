package org.molr.commons.domain.dto;

import com.google.common.collect.ImmutableSet;
import org.molr.commons.domain.MissionParameter;
import org.molr.commons.domain.MissionParameterDescription;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

public class MissionParameterDescriptionDto {

    public final Set<MissionParameterDto> parameters;

    public MissionParameterDescriptionDto() {
        this.parameters = Collections.emptySet();
    }

    private MissionParameterDescriptionDto(Set<MissionParameterDto> missionParameters) {
        this.parameters = Objects.requireNonNull(missionParameters, "missionParameters must not be null");
    }

    public static final MissionParameterDescriptionDto from(MissionParameterDescription description) {
        Set<MissionParameterDto> parameterDtos = description.parameters().stream().map(MissionParameterDto::from).collect(toSet());
        return new MissionParameterDescriptionDto(parameterDtos);
    }

    public MissionParameterDescription toMissionParameterDescription() {
        /* this seems to be complicatedly achievable with streams... so we do the transformation here in a more classical way*/
        ImmutableSet.Builder<MissionParameter<?>> builder = ImmutableSet.builder();
        this.parameters.forEach(p -> builder.add(p.toMissionParameter()));
        return new MissionParameterDescription(builder.build());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionParameterDescriptionDto that = (MissionParameterDescriptionDto) o;
        return Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters);
    }

    @Override
    public String toString() {
        return "MissionParameterDescriptionDto{" +
                "parameters=" + parameters +
                '}';
    }
}
