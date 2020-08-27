package io.molr.commons.domain;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

import java.util.Map;

public final class MissionParameterDescription {

    private final Set<MissionParameter<?>> parameters;
    
    public final Map<String, ParameterRestriction> restrictions;

    public MissionParameterDescription(Set<MissionParameter<?>> parameters) {
        this.restrictions = null;
        this.parameters = ImmutableSet.copyOf(requireNonNull(parameters, "parameters must not be null"));
    }
    
    public MissionParameterDescription(Set<MissionParameter<?>> parameters, Map<String, ParameterRestriction> restrictions) {
        this.restrictions = restrictions;
        this.parameters = ImmutableSet.copyOf(requireNonNull(parameters, "parameters must not be null"));
    }

    public Set<MissionParameter<?>> parameters() {
        return this.parameters;
    }

    public boolean hasParameterForPlaceholder(Placeholder<?> placeholder) {
        return parameters.stream().anyMatch(parameter -> parameter.placeholder().equals(placeholder));
    }

    public static final MissionParameterDescription empty() {
        return new MissionParameterDescription(ImmutableSet.of());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionParameterDescription that = (MissionParameterDescription) o;
        return Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameters);
    }

    @Override
    public String toString() {
        return "MissionParameterDescription{" +
                "parameters=" + parameters +
                '}';
    }
}
