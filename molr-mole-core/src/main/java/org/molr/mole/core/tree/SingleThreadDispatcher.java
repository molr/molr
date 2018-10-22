package org.molr.mole.core.tree;

import org.molr.commons.domain.StrandCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SingleThreadDispatcher extends CommandDispatcher{

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleThreadDispatcher.class);

    private final AtomicReference<StrandCommand> nextCommand = new AtomicReference<>(null);

    private final ExecutorService executorService;

    public SingleThreadDispatcher(Consumer<StrandCommand> consumer, ExecutorService executorService1) {
        super(consumer);
        this.executorService = executorService1;
        executorService.submit(this::start);
    }

    private void start() {
        /* Has to go onto the correct thread */
        while (true) {
            StrandCommand command = nextCommand.getAndSet(null);
            if (command != null) {
                consumer().accept(command);
            } else {
                sleep(10);
            }

        }
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void instruct(StrandCommand command) {
        boolean didUpdate = nextCommand.compareAndSet(null, command);
        if(!didUpdate) {
            LOGGER.warn("Could not accept command {}", command);
        }
    }




}
