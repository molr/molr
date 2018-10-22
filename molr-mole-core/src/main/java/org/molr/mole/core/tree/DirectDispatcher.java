package org.molr.mole.core.tree;

import org.molr.commons.domain.StrandCommand;

import java.util.function.Consumer;

public class DirectDispatcher extends  CommandDispatcher {

    public DirectDispatcher(Consumer<StrandCommand> consumer) {
        super(consumer);
    }

    @Override
    public void instruct(StrandCommand command) {
        consumer().accept(command);
    }
}
