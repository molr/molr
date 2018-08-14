/**
 * Copyright (c) 2017 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package org.molr.commons.api.domain;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Block {

    private final String id;
    private final String text;
    private final List<String> childrenBlockIds;
    private final boolean navigable;

    public Block(Builder builder) {
        this.id = builder.id;
        this.text = builder.text;
        this.navigable = builder.navigable;
        this.childrenBlockIds = ImmutableList.copyOf(builder.children);
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

    public List<String> childrenBlockIds() {
        return this.childrenBlockIds;
    }

    public boolean isNavigable() {
        return this.navigable;
    }

    public static class Builder {
        private final String id;
        private final String text;
        private List<String> children = new ArrayList<>();
        private boolean navigable = false;

        Builder(String id, String text) {
            this.id = requireNonNull(id, "id must not be null");
            this.text = requireNonNull(text, "text must not be null");
        }

        public Builder child(Block block) {
            requireNonNull(block, "child block must not be null");
            this.children.add(block.id);
            return this;
        }

        public Builder childrenIds(List<String> ids) {
            requireNonNull(ids, "ids  must not be null");
            this.children.addAll(ids);
            return this;
        }

        public Builder childId(String childBlockId) {
            requireNonNull(childBlockId, "child block id must not be null");
            this.children.add(childBlockId);
            return this;
        }

        public Builder navigable(boolean navigable) {
            this.navigable = navigable;
            return this;
        }

        public Builder navigable() {
            return navigable(true);
        }

        public Block build() {
            return new Block(this);
        }
    }

    public static final Block idAndText(String id, String text) {
        return builder(id, text).build();
    }


}
