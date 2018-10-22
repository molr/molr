package org.molr.mole.core.tree;

import org.molr.commons.domain.Strand;

import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class MutableStrandTracker {

    private final Map<Strand, Stack<SequentialExecutor>> tracker = new ConcurrentHashMap<>();

    public void trackStrand(Strand strand) {
        if (tracker.containsKey(strand)) {
            throw new IllegalArgumentException(strand + " is already managed by this tracker");
        }
        tracker.put(strand, new Stack<>());
    }

    public void setCurrentExecutorFor(Strand strand, SequentialExecutor executor) {
        Stack<SequentialExecutor> executors = tracker.get(strand);
        if(executors == null) {
            throw new IllegalArgumentException(strand + " is not managed by this tracker");
        }
        // ????
        synchronized (executors) {
            executors.push(executor);
        }
    }

    public void unsetCurrentExecutorFor(Strand strand, SequentialExecutor executor) {
        Stack<SequentialExecutor> executors = tracker.get(strand);
        if(executors == null) {
            throw new IllegalArgumentException(strand + " is not managed by this tracker");
        }
        // ????
        synchronized (executors) {
            if(!executors.peek().equals(executor)) {
                throw new IllegalArgumentException("Trying to unset current executor for " + strand + " but the strand is pointing at another executor");
            }
            executors.pop();
        }
    }

    public SequentialExecutor currentExecutorFor(Strand strand) {
        Stack<SequentialExecutor> executors = tracker.get(strand);
        if(executors == null) {
            throw new IllegalArgumentException(strand + " is not managed by this tracker");
        }

        return executors.peek();
    }
}
