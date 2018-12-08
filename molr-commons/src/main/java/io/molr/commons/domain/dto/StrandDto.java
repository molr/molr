package io.molr.commons.domain.dto;

import io.molr.commons.domain.Strand;

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
