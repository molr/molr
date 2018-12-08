package io.molr.commons.domain;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class MissionParameter<T> {

    private final Placeholder<T> placeholder;
    private final boolean required;
    private final T defaultValue;

    private MissionParameter(Placeholder<T> placeholder, T defaultValue, boolean required) {
        this.placeholder = requireNonNull(placeholder, "placeholder must not be null");
        this.required = required;
        /* null is allowed for the default value*/
        this.defaultValue = defaultValue;
    }

    public static <T> MissionParameter<T> required(Placeholder<T> placeholder) {
        return new MissionParameter<T>(placeholder, null, true);
    }

    public static <T> MissionParameter<T> optional(Placeholder<T> placeholder) {
        return new MissionParameter<T>(placeholder, null, false);
    }

    public MissionParameter<T> withDefault(T newDefaultValue) {
        return new MissionParameter<>(placeholder, newDefaultValue, this.required);
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionParameter<?> that = (MissionParameter<?>) o;
        return required == that.required &&
                Objects.equals(placeholder, that.placeholder) &&
                Objects.equals(defaultValue, that.defaultValue);
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
                '}';
    }
}
