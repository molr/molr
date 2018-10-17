/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.domain;

import java.util.Objects;

/**
 * Identifies a mission.
 * <p>
 * This is a stub. Most probably more things have to be added here ... e.g. som id which guarantees uniqueness..
 * 
 * @author kfuchsbe
 */
public class Mission {

    private final String name;

    public Mission(String name) {
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Mission other = (Mission) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Mission [name=" + name + "]";
    };

}
