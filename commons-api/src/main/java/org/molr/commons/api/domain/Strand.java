/**
 * Copyright (c) 2018 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.api.domain;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * One 'story line' within a mission. A mission might split up in different strands, of which each of them can have its
 * own state (e.g. running, paused ...). (If you would like to relate this to java, you would probably imagine it as a
 * Thread ;-)
 *
 * @author kfuchsbe
 */
public class Strand {

    private final long id;
    private final String name;

    private final Strand parent;

    private Strand(long id, String name) {
        this.id = id;
        this.name = requireNonNull(name, "name must not be null");
        this.parent = null;
    }

    private Strand(long id, String name, Strand parent) {
        this.id = id;
        this.name = requireNonNull(name, "name must not be null");
        this.parent = requireNonNull(parent, "parent must not be null");
    }

    public static Strand ofIdName(long id, String name) {
        return new Strand(id, name);
    }

    public static Strand ofIdNameParent(long id, String name, Strand parent) {
        return new Strand(id, name, parent);
    }

    public String name() {
        return name;
    }

    public long id() {
        return id;
    }

    public Optional<Strand> parent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Strand strand = (Strand) o;
        return id == strand.id &&
                Objects.equals(name, strand.name) &&
                Objects.equals(parent, strand.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, parent);
    }

    @Override
    public String toString() {
        return "Strand{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parent=" + parent +
                '}';
    }
}
