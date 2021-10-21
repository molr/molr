package io.molr.mole.core.tree;

import java.util.function.Consumer;

import io.molr.commons.domain.StrandCommand;

public abstract class CommandDispatcher {

    private final Consumer<StrandCommand> consumer;

    public CommandDispatcher(Consumer<StrandCommand> consumer) {
        this.consumer = consumer;
    }

    protected Consumer<StrandCommand> consumer() {
        return this.consumer;
    }

    public abstract void instruct(StrandCommand command);
}
