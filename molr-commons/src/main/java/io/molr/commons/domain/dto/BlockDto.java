package io.molr.commons.domain.dto;

import io.molr.commons.domain.Block;

public class BlockDto {

    public final String id;
    public final String text;

    public BlockDto(String id, String text) {
        this.id = id;
        this.text = text;
    }

    public BlockDto() {
        this.id = null;
        this.text = null;
    }

    public static final BlockDto from(Block block) {
        return new BlockDto(block.id(), block.text());
    }

    public Block toBlock() {
        return Block.builder(this.id, this.text).build();
    }


    @Override
    public String toString() {
        return "BlockDto{" +
                "id='" + id + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
