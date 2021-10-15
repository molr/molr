package io.molr.mole.core.tree;

import java.util.Optional;

import io.molr.commons.domain.Strand;

public interface StrandFactory {

    Strand createChildStrand(Strand parent);

    Strand rootStrand();

    Optional<Strand> parentOf(Strand strand);
}
