package org.molr.commons.api.domain.dto;

import org.molr.commons.api.domain.Block;

import java.util.List;

public class BlockDto {

    public final String id;
    public final String text;
    public final boolean navigable;
    public final List<String> childBlockIds;

    public BlockDto(String id, String text, boolean navigable, List<String> childBlockIds) {
        this.id = id;
        this.text = text;
        this.navigable = navigable;
        this.childBlockIds = childBlockIds;
    }

    public BlockDto() {
        this.id = null;
        this.text = null;
        this.navigable = false;
        this.childBlockIds = null;
    }

    public static final BlockDto from(Block block) {
        return new BlockDto(block.id(), block.text(), block.isNavigable(), block.childrenBlockIds());
    }

    public Block toBlock() {
        return Block.builder(this.id, this.text).childrenIds(this.childBlockIds).navigable(this.navigable).build();
    }

    @Override
    public String toString() {
        return "BlockDto{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", navigable=" + navigable +
                ", childBlockIds=" + childBlockIds +
                '}';
    }
}
