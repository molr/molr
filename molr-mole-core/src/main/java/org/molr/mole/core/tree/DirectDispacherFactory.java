package org.molr.mole.core.tree;

import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;

import java.util.function.Consumer;

public class DirectDispacherFactory implements DispacherFactory {

    @Override
    public CommandDispatcher createDispatcher(Strand strand, Consumer<StrandCommand> command) {
        return new DirectDispatcher(command);
    }

}
