package io.molr.commons.domain.dto;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.MissionParameter;
import io.molr.commons.domain.Placeholder;

import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.function.Function;

import static io.molr.commons.util.Exceptions.illegalArgumentException;
import static io.molr.commons.util.Exceptions.illegalStateException;

public class MissionParameterDto<T> {

    private static final BiMap<Class<?>, String> TYPE_NAMES = ImmutableBiMap.of(
            String.class, "string",
            Double.class, "double",
            Integer.class, "integer",
            Boolean.class, "boolean"
    );
    private static final Map<Class<?>, Function<String, Placeholder<?>>> TYPE_CREATORS = ImmutableMap.of(
            String.class, Placeholder::aString,
            Double.class, Placeholder::aDouble,
            Integer.class, Placeholder::anInteger,
            Boolean.class, Placeholder::aBoolean
    );

    public final String name;
    public final String type;
    public final boolean required;
    public final T defaultValue;
    public final Set<T> allowedValues;

    public MissionParameterDto() {
        this.name = null;
        this.type = null;
        this.required = false;
        this.defaultValue = null;
        this.allowedValues = ImmutableSet.of();
    }

    public MissionParameterDto(String name, String type, boolean required, T defaultValue, Set<T> allowedValues) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.required = required;
        this.defaultValue = defaultValue;
        this.allowedValues = ImmutableSet.copyOf(requireNonNull(allowedValues,"allowedValues must not be null"));        
    }

    public static final <T> MissionParameterDto<T> from(MissionParameter<T> parameter) {
        Placeholder<T> placeholder = parameter.placeholder();
        MissionParameterDto<T> dto = new MissionParameterDto<>(placeholder.name(), typeStringFrom(placeholder.type()),
                parameter.isRequired(), parameter.defaultValue(), parameter.allowedValues());
        return dto;
    }

    public MissionParameter<T> toMissionParameter() {
        if (this.required) {
            return MissionParameter.required(placeholder()).withDefault(defaultValue).withAllowed(allowedValues);
        } else {
            return MissionParameter.optional(placeholder()).withDefault(defaultValue).withAllowed(allowedValues);
        }
    }

    private Placeholder<T> placeholder() {
        Class<?> typeClass = TYPE_NAMES.inverse().get(type);
        if (typeClass == null) {
            throw illegalStateException("Type '{}' cannot be converted into a valid java type.", type);
        }
        Function<String, Placeholder<?>> typeSupplier = TYPE_CREATORS.get(typeClass);
        if (typeSupplier == null) {
            throw illegalStateException("Type '{}' cannot be converted into a valid java type.", type);
        }
        return (Placeholder<T>) typeSupplier.apply(name);
    }

    private static final String typeStringFrom(Class<?> type) {
        String typeName = TYPE_NAMES.get(type);
        if (typeName != null) {
            return typeName;
        }
        throw illegalArgumentException("Type '{}' cannot be mapped to a valid json value.", type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionParameterDto that = (MissionParameterDto) o;
        return required == that.required &&
                Objects.equals(name, that.name) &&
                Objects.equals(type, that.type) &&
                Objects.equals(defaultValue, that.defaultValue) &&
                Objects.equals(allowedValues, that.allowedValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, required, defaultValue, allowedValues);
    }

    @Override
    public String toString() {
        return "MissionParameterDto{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", required=" + required +
                ", defaultValue=" + defaultValue +
                ", allowedValues=" + allowedValues +
                '}';
    }
}