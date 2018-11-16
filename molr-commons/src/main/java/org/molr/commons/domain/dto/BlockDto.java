package org.molr.commons.domain.dto;

import org.molr.commons.domain.Block;

public class BlockDto {

    public final String id;
    public final String text;
    public final boolean navigable;

    public BlockDto(String id, String text, boolean navigable) {
        this.id = id;
        this.text = text;
        this.navigable = navigable;
    }

    public BlockDto() {
        this.id = null;
        this.text = null;
        this.navigable = false;
    }

    public static final BlockDto from(Block block) {
        return new BlockDto(block.id(), block.text(), block.isNavigable());
    }

    public Block toBlock() {
        return Block.builder(this.id, this.text).navigable(this.navigable).build();
    }


    @Override
    public String toString() {
        return "BlockDto{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                ", navigable=" + navigable +
                '}';
    }
}
