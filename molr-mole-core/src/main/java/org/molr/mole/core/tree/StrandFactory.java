package org.molr.mole.core.tree;

import org.molr.commons.domain.Strand;

import java.util.Optional;

public interface StrandFactory {

    Strand createChildStrand(Strand parent);

    Strand rootStrand();

    Optional<Strand> parentOf(Strand strand);
}
