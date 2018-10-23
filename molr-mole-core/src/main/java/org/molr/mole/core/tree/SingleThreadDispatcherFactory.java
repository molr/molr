package org.molr.mole.core.tree;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.molr.commons.domain.Strand;
import org.molr.commons.domain.StrandCommand;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class SingleThreadDispatcherFactory implements DispacherFactory {

    private final ConcurrentHashMap<Strand, ExecutorService> strandExecutors = new ConcurrentHashMap<>();

    @Override
    public CommandDispatcher createDispatcher(Strand strand, Consumer<StrandCommand> command) {
        return new SingleThreadDispatcher(command, singleThreadExecutor(strand));
    }

    private ExecutorService singleThreadExecutor(Strand strand) {
        return newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("command-dispatcher-" + strand.id() + "-thread").build());
    }
}
