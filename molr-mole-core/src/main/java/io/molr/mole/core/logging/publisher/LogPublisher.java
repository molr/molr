package io.molr.mole.core.logging.publisher;

import io.molr.mole.core.logging.consumer.LogConsumer;

import java.util.Observable;

// TODO Add javadoc
public class LogPublisher extends Observable {
    public void addConsumer(LogConsumer consumer) {
        addObserver(consumer);
    }

    public void publish(Object msg) {
        setChanged();
        notifyObservers(msg);
    }
}
