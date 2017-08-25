/*
 * © Copyright 2016 CERN. This software is distributed under the terms of the Apache License Version 2.0, copied
 * verbatim in the file “COPYING“.ing this licence, CERN does not waive the privileges and immunities granted
 * to it by virtue of its status as an Intergovernmental Organization or submit itself to any jurisdiction.
 */

package cern.molr.inspector.controller;

import cern.molr.inspector.entry.EntryListener;
import com.sun.jdi.ThreadReference;

import java.util.Optional;

/**
 * A registry for entries in a JDI instance.
 *
 * @param <Listener> The type of {@link EntryListener}s the registry stores.
 */
public class JdiEntryRegistry<Listener extends EntryListener> {

    private Optional<ThreadReference> thread = Optional.empty();
    private Optional<Listener> listener = Optional.empty();

    public Optional<ThreadReference> getThreadReference() {
        return thread;
    }

    public Optional<Listener> getEntryListener() {
        return listener;
    }

    public void register(ThreadReference reference, Listener listener) {
        if (thread.isPresent()) {
            throw new IllegalStateException("Entry already registered");
        }

        this.thread = Optional.ofNullable(reference);
        this.listener = Optional.ofNullable(listener);
    }

    public void unregister() {
        this.thread = Optional.empty();
        this.listener = Optional.empty();
    }

}