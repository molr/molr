/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.api.domain;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Block {

    private final String text;
    private final List<Block> children;
    private final boolean navigable;

    public Block(Builder builder) {
        this.text = builder.text;
        this.navigable = builder.navigable;
        this.children = ImmutableList.copyOf(builder.children);
    }

    public static final Builder builder(long id, String text) {
        return new Builder(id, text);
    }

    public String text() {
        return this.text;
    }

    public List<Block> children() {
        return this.children;
    }

    public boolean isNavigable() {
        return this.navigable;
    }

    public static class Builder {
        private final long id;
        private final String text;
        private List<Block> children = new ArrayList<>();
        private boolean navigable = false;

        Builder(long id, String text) {
            this.id = id;
            this.text = requireNonNull(text, "text must not be null");
        }

        public Builder child(Block block) {
            requireNonNull(block, "child block must not be null");
            this.children.add(block);
            return this;
        }

        public Builder navigable() {
            this.navigable = true;
            return this;
        }

        public Block build() {
            return new Block(this);
        }
    }

    public static final Block idAndText(long id, String text) {
        return builder(id, text).build();
    }


}
