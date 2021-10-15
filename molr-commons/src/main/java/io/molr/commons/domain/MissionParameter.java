package io.molr.commons.domain;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public final class MissionParameter<T> {

    private final Placeholder<T> placeholder;
    private final boolean required;
    private final T defaultValue;
    private Set<T> allowedValues;
    /*
     * Map for additional information such as tags, schemas etc.
     */
    private Map<String, Object> meta;

    private MissionParameter(Placeholder<T> placeholder, T defaultValue, boolean required, Set<T> allowedValues, Map<String, Object> meta) {
        this.placeholder = requireNonNull(placeholder, "placeholder must not be null");
        this.required = required;
        /* null is allowed for the default value*/
        this.defaultValue = defaultValue;
        this.allowedValues = requireNonNull(allowedValues, "allowedValues must not be null");
        this.meta = meta;
    }

    public static <T> MissionParameter<T> required(Placeholder<T> placeholder) {
        return new MissionParameter<>(placeholder, null, true, ImmutableSet.of(), ImmutableMap.of());
    }

    public static <T> MissionParameter<T> optional(Placeholder<T> placeholder) {
        return new MissionParameter<>(placeholder, null, false, ImmutableSet.of(), ImmutableMap.of());
    }

    public MissionParameter<T> withDefault(T newDefaultValue) {
        return new MissionParameter<>(placeholder, newDefaultValue, this.required, allowedValues, meta);
    }

    public MissionParameter<T> withAllowed(Collection<T> newAllowedValues) {
        return new MissionParameter<>(placeholder, defaultValue, required, ImmutableSet.copyOf(newAllowedValues), meta);
    }
    
    public MissionParameter<T> withMeta(Map<String, Object> newMeta){
    	return new MissionParameter<>(placeholder, defaultValue, required, allowedValues, newMeta);
    }
    
    public boolean isRequired() {
        return this.required;
    }

    public T defaultValue() {
        return this.defaultValue;
    }

    public Placeholder<T> placeholder() {
        return this.placeholder;
    }
    
    public Set<T> allowedValues(){
        return new ImmutableSet.Builder<T>().addAll(allowedValues).build();
    }

    public Map<String, Object> meta(){
    	return this.meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionParameter<?> that = (MissionParameter<?>) o;
        return required == that.required &&
                Objects.equals(placeholder, that.placeholder) &&
                Objects.equals(defaultValue, that.defaultValue) &&
                Objects.equals(allowedValues, that.allowedValues) &&
                Objects.equals(meta, that.meta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeholder, required, defaultValue, meta);
    }

    @Override
    public String toString() {
        return "MissionParameter{" +
                "placeholder=" + placeholder +
                ", required=" + required +
                ", defaultValue=" + defaultValue +
                ", allowedValues=" + allowedValues +
                ", meta=" +meta +
                '}';
    }
}
