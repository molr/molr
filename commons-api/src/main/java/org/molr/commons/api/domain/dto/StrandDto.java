package org.molr.commons.api.domain.dto;

import org.molr.commons.api.domain.Strand;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class StrandDto {

    public final String id;

    public StrandDto(String id) {
        this.id = requireNonNull(id, "id must not be null");
    }

    public StrandDto() {
        this.id = null;
    }

    @Override
    public String toString() {
        return "StrandDto{" +
                "id='" + id + '\'' +
                '}';
    }

    public static StrandDto from(Strand strand) {
        return new StrandDto(strand.id());
    }

    public Strand toStrand() {
        return Strand.ofId(id);
    }
}
