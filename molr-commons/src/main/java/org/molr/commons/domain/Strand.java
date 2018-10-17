/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.domain;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * One 'story line' within a mission. A mission might split up in different strands, of which each of them can have its
 * own state (e.g. running, paused ...). (If you would like to relate this to java, you would probably imagine it as a
 * Thread ;-)
 *
 * @author kfuchsbe
 */
public class Strand {

    private final String id;


    private Strand(String id) {
        this.id = requireNonNull(id, "id must not be null");
    }


    public static Strand ofId(String id) {
        return new Strand(id);
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Strand strand = (Strand) o;
        return Objects.equals(id, strand.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Strand{" +
                "id='" + id + '\'' +
                '}';
    }
}
