package io.molr.commons.domain.dto;

import static io.molr.commons.util.Exceptions.illegalArgumentException;
import static io.molr.commons.util.Exceptions.illegalStateException;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import io.molr.commons.domain.ListOfStrings;
import io.molr.commons.domain.MissionParameter;
import io.molr.commons.domain.Placeholder;

public class MissionParameterDto<T> {

    public static final String TYPE_STRING = "string";
    public static final String TYPE_DOUBLE = "double";
    public static final String TYPE_INTEGER = "integer";
    public static final String TYPE_LONG = "long";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_STRING_ARRAY = "string[]";
    public static final String TYPE_LIST_OF_STRINGS = "listOfStrings";
    /*
     * custom types can be registered
     */
        
    /*
     *TODO find a better way and or place for type registration?
     */
    public static final BiMap<Class<?>, String> TYPE_NAMES = HashBiMap.create();
    static {
        TYPE_NAMES.put(String.class, TYPE_STRING);
        TYPE_NAMES.put(Double.class, TYPE_DOUBLE);
        TYPE_NAMES.put(Integer.class, TYPE_INTEGER);
        TYPE_NAMES.put(Long.class, TYPE_LONG);
        TYPE_NAMES.put(Boolean.class, TYPE_BOOLEAN);
        TYPE_NAMES.put(String[].class, TYPE_STRING_ARRAY);
        TYPE_NAMES.put(ListOfStrings.class, TYPE_LIST_OF_STRINGS);
    }
    
    public static final Map<Class<?>, Function<String, Placeholder<?>>> TYPE_CREATORS = Maps.newHashMap();
    static {
        TYPE_CREATORS.put(String.class, Placeholder::aString);
        TYPE_CREATORS.put(Double.class, Placeholder::aDouble);
        TYPE_CREATORS.put(Integer.class, Placeholder::anInteger);
        TYPE_CREATORS.put(Long.class, Placeholder::aLong);
        TYPE_CREATORS.put(Boolean.class, Placeholder::aBoolean);
        TYPE_CREATORS.put(String[].class, Placeholder::aStringArray);
        TYPE_CREATORS.put(ListOfStrings.class, Placeholder::aListOfStrings);
    }

    public final String name;
    public final String type;
    public final boolean required;
    public final T defaultValue;
    public final Set<T> allowedValues;
    public final Map<String, Object> meta;

    public MissionParameterDto() {
        this.name = null;
        this.type = null;
        this.required = false;
        this.defaultValue = null;
        this.allowedValues = ImmutableSet.of();
        this.meta = ImmutableMap.of();
    }

    public MissionParameterDto(String name, String type, boolean required, T defaultValue, Set<T> allowedValues, Map<String, Object> meta) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.required = required;
        this.defaultValue = defaultValue;
        this.allowedValues = ImmutableSet.copyOf(requireNonNull(allowedValues, "allowedValues must not be null"));
        this.meta = meta;
    }

    public static final <T> MissionParameterDto<T> from(MissionParameter<T> parameter) {
        Placeholder<T> placeholder = parameter.placeholder();
        MissionParameterDto<T> dto = new MissionParameterDto<>(placeholder.name(), typeStringFrom(placeholder.type()),
                parameter.isRequired(), parameter.defaultValue(), parameter.allowedValues(), parameter.meta());
        return dto;
    }

    public MissionParameter<T> toMissionParameter() {
        if (this.required) {
            return MissionParameter.required(placeholder()).withDefault(defaultValue).withAllowed(allowedValues).withMeta(meta);
        } 
            return MissionParameter.optional(placeholder()).withDefault(defaultValue).withAllowed(allowedValues).withMeta(meta);
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
        MissionParameterDto<?> that = (MissionParameterDto<?>) o;
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