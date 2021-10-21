/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package io.molr.commons.domain;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

public final class Block {

    private final String id;
    private final String text;
    private final boolean navigable;

    public Block(Builder builder) {
        this.id = builder.id;
        this.text = builder.text;
        this.navigable = builder.navigable;
    }

    public static final Builder builder(String id, String text) {
        return new Builder(id, text);
    }

    public String id() {
        return this.id;
    }

    public String text() {
        return this.text;
    }

    public boolean isNavigable() {
        return this.navigable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return navigable == block.navigable &&
                Objects.equals(id, block.id) &&
                Objects.equals(text, block.text);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, text, navigable);
    }

    public static class Builder {
        private final String id;
        private final String text;
        private boolean navigable = false;

        Builder(String id, String text) {
            this.id = requireNonNull(id, "id must not be null");
            this.text = requireNonNull(text, "text must not be null");
        }
        
        public Builder navigable(boolean newNavigable) {
            this.navigable = newNavigable;
            return this;
        }

        public Block build() {
            return new Block(this);
        }
    }

    public static final Block idAndText(String id, String text) {
        return builder(id, text).build();
    }

    @Override
    public String toString() {
        return "Block{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", navigable=" + navigable +
                '}';
    }

}
