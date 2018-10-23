package org.molr.mole.core.tree;

import org.molr.commons.domain.Strand;

public interface StrandFactory {

    Strand createChildStrand(Strand parent);

    Strand rootStrand();

    Strand parentOf(Strand strand);
}
