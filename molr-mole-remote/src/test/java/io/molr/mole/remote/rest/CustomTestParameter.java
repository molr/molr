package io.molr.mole.remote.rest;

import java.util.List;

import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.dto.MissionParameterDto;

/**
 *
 * @author krepp
 */
public class CustomTestParameter {

    static {
        MissionParameterDto.TYPE_CREATORS.put(CustomTestParameter.class, (name)->{
            return Placeholder.of(CustomTestParameter.class, name);
        });
        MissionParameterDto.TYPE_NAMES.put(CustomTestParameter.class, "myCustomType");
    }
    
    long value;
    
    String name;
    
    List<String> someStrings;

    public CustomTestParameter() {
        
    }
    
    public CustomTestParameter(long value, String name, List<String> someStrings) {
        this.value = value;
        this.name = name;
        this.someStrings = someStrings;
    }

    /**
     * @return the value
     */
    public long getValue() {
        return this.value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(long value) {
        this.value = value;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the someStrings
     */
    public List<String> getSomeStrings() {
        return this.someStrings;
    }

    /**
     * @param someStrings the someStrings to set
     */
    public void setSomeStrings(List<String> someStrings) {
        this.someStrings = someStrings;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        result = prime * result + ((this.someStrings == null) ? 0 : this.someStrings.hashCode());
        result = prime * result + (int) (this.value ^ (this.value >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomTestParameter other = (CustomTestParameter) obj;
        if (this.name == null) {
            if (other.name != null)
                return false;
        } else if (!this.name.equals(other.name))
            return false;
        if (this.someStrings == null) {
            if (other.someStrings != null)
                return false;
        } else if (!this.someStrings.equals(other.someStrings))
            return false;
        if (this.value != other.value)
            return false;
        return true;
    }

}

