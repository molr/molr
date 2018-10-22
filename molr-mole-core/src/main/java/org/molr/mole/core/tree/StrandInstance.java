package org.molr.mole.core.tree;

import org.molr.commons.domain.Strand;

import java.util.concurrent.CompletableFuture;

public interface StrandInstance {

    Strand strand();

    CompletableFuture<Void> run(Runnable runnable);
}
