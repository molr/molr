package io.molr.commons.domain;

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import static java.util.Objects.requireNonNull;

import java.util.Collection;

public final class MissionParameter<T> {

    private final Placeholder<T> placeholder;
    private final boolean required;
    private final T defaultValue;
    private Set<T> allowedValues;

    private MissionParameter(Placeholder<T> placeholder, T defaultValue, boolean required, Collection<T> allowedValues) {
        this.placeholder = requireNonNull(placeholder, "placeholder must not be null");
        this.required = required;
        /* null is allowed for the default value*/
        this.defaultValue = defaultValue;
        this.allowedValues = Sets.newHashSet();
        if(allowedValues != null) {
            this.allowedValues.addAll(allowedValues);
        }
    }

    public static <T> MissionParameter<T> required(Placeholder<T> placeholder) {
        return new MissionParameter<>(placeholder, null, true, null);
    }

    public static <T> MissionParameter<T> optional(Placeholder<T> placeholder) {
        return new MissionParameter<>(placeholder, null, false, null);
    }

    public MissionParameter<T> withDefault(T newDefaultValue) {
        return new MissionParameter<>(placeholder, newDefaultValue, this.required, allowedValues);
    }

    public MissionParameter<T> withAllowed(Collection<T> newAllowedValues) {
        return new MissionParameter<>(placeholder, defaultValue, required, newAllowedValues);
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionParameter<?> that = (MissionParameter<?>) o;
        return required == that.required &&
                Objects.equals(placeholder, that.placeholder) &&
                Objects.equals(defaultValue, that.defaultValue) &&
                Objects.equals(allowedValues, that.allowedValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeholder, required, defaultValue);
    }

    @Override
    public String toString() {
        return "MissionParameter{" +
                "placeholder=" + placeholder +
                ", required=" + required +
                ", defaultValue=" + defaultValue +
                ", allowedValues=" + allowedValues +
                '}';
    }
}
