package org.molr.commons.domain.dto;

import com.google.common.collect.ImmutableSet;
import org.molr.commons.domain.MissionParameter;
import org.molr.commons.domain.MissionParameterDescription;

import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class MissionParameterDescriptionDto {

    public final Set<MissionParameterDto> parameters;

    public MissionParameterDescriptionDto(Set<MissionParameterDto> missionParameters) {
        this.parameters = Objects.requireNonNull(missionParameters, "missionParameters must not be null");
    }

    public static final MissionParameterDescriptionDto from(MissionParameterDescription description) {
        ImmutableSet<MissionParameterDto> parameterDtos = description.parameters().stream().map(MissionParameterDto::from).collect(toImmutableSet());
        return new MissionParameterDescriptionDto(parameterDtos);
    }

    public MissionParameterDescription toMissionParameterDescription() {
        ImmutableSet<MissionParameter<?>> parameters = this.parameters.stream().map(MissionParameterDto::toMissionParameter).collect(toImmutableSet());
        return new MissionParameterDescription(parameters);
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
