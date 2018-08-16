package org.molr.commons.api.domain.dto;

import org.molr.commons.api.domain.Strand;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class StrandDto {

    public final String id;
    public final String parentId;

    public StrandDto(String id, String parentId) {
        this.id = requireNonNull(id, "id must not be null");
        this.parentId = parentId;
    }

    public StrandDto() {
        this.id = null;
        this.parentId = null;
    }

    @Override
    public String toString() {
        return "StrandDto{" +
                "id='" + id + '\'' +
                '}';
    }

    public static StrandDto from(Strand strand) {
        return new StrandDto(strand.id(), strand.parentId().orElse(null));
    }

    public Strand toStrand() {
        if (parentId == null) {
            return Strand.ofId(id);
        } else {
            return Strand.ofIdAndParentId(id, parentId);
        }
    }
}
