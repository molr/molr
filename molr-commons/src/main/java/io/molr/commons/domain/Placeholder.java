package io.molr.commons.domain;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Placeholder<T> {

    private final static Logger LOGGER = LoggerFactory.getLogger(Placeholder.class);
    
    private final Class<T> type;
    private final String name;
    private Function<Object, T> converter = null;

    private Placeholder(Class<T> type, String name) {
        this.type = requireNonNull(type, "type must not be null");
        this.name = requireNonNull(name, "name must not be null");
    }
    
    private Placeholder(Class<T> type, String name, Function<Object, T> converter) {
        this.type = requireNonNull(type, "type must not be null");
        this.name = requireNonNull(name, "name must not be null");
        this.converter = converter;
    }

    public static final Placeholder<Double> aDouble(String name) {
        return new Placeholder<>(Double.class, name);
    }

    public static final Placeholder<Boolean> aBoolean(String name) {
        return new Placeholder<>(Boolean.class, name);
    }

    /*
     * The used conversion workaround would be unnecessary if types are 
     * deserialized correctly.
     */
    public static final Placeholder<Long> aLong(String name) {
        return new Placeholder<>(Long.class, name, longOrInt -> {
            if(longOrInt.getClass().equals(Integer.class)){
                return Long.valueOf((Integer)longOrInt);
            }
            return (Long)longOrInt;
        });
    }

    public static final Placeholder<Integer> anInteger(String name) {
        return new Placeholder<>(Integer.class, name);
    }

    public static final Placeholder<String> aString(String name) {
        return new Placeholder<>(String.class, name);
    }
    
    @SuppressWarnings("unchecked")
    public static final Placeholder<String[]> aStringArray(String name) {
        return new Placeholder<>(String[].class, name, o-> {
            if(o.getClass().equals(ArrayList.class)){
                LOGGER.warn("Converter needs to convert ArrayList into String[] "+o);
                ArrayList<String> strings = (ArrayList<String>)o;
                String[] stringArray = strings.toArray(new String[strings.size()]);
                return stringArray;
            }
            if(o.getClass().equals(String[].class)){
                return (String[])o;
            }
            throw new IllegalArgumentException("object "+o+" of type "+o.getClass()+" cannot be converted to String[]");
            
        });
    }
    
    public static final Placeholder<ListOfStrings> aListOfStrings(String name) {
        return new Placeholder<>(ListOfStrings.class, name, (o) -> {
            if(o.getClass().equals(ArrayList.class)) {
                LOGGER.warn("Converter needs to convert ArrayList into list of type ListOfStrings.class: "+o);
            }
          return new ListOfStrings((ArrayList<String>)o);  
        });
    }

    public Class<T> type() {
        return this.type;
    }

    public String name() {
        return this.name;
    }
    
    /**
     * This is just a workaround for types that are not converted at the remote interface
     * @return a function that converts or casts an object to the type specified by the placeholder
     */
    public Function<Object, T> caster(){
        if(converter != null) {
            return converter;
        }
        return o -> type.cast(o);
    }

    public static final <T> Placeholder<T> of(Class<T> type, String name) {
        return new Placeholder<>(type, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Placeholder<?> that = (Placeholder<?>) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public String toString() {
        return "Placeholder{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
