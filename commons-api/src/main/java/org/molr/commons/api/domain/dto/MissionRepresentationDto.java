package org.molr.commons.api.domain.dto;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import org.molr.commons.api.domain.Block;
import org.molr.commons.api.domain.ImmutableMissionRepresentation;
import org.molr.commons.api.domain.MissionRepresentation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class MissionRepresentationDto {

    public final String rootBlockId;
    public final Set<BlockDto> blocks;
    public final Map<String, List<String>> blockChildrenIds;

    public MissionRepresentationDto(String rootBlockId, Set<BlockDto> blocks, Map<String, List<String>> blockChildrenIds) {
        this.rootBlockId = rootBlockId;
        this.blocks = blocks;
        this.blockChildrenIds = blockChildrenIds;
    }

    public MissionRepresentationDto() {
        this.rootBlockId = null;
        this.blocks = Collections.emptySet();
        this.blockChildrenIds = Collections.emptyMap();
    }

    public static final MissionRepresentationDto from(MissionRepresentation representation) {
        Set<Block> allBlocks = representation.allBlocks();
        Set<BlockDto> blockDtos = allBlocks.stream().map(BlockDto::from).collect(toSet());

        ImmutableMap.Builder<String, List<String>> builder = ImmutableMap.builder();
        for (Block block : allBlocks) {
            List<Block> children = representation.childrenOf(block);
            if (!children.isEmpty()) {
                List<String> childrenIds = children.stream().map(Block::id).collect(toList());
                builder.put(block.id(), childrenIds);
            }
        }
        return new MissionRepresentationDto(representation.rootBlock().id(), blockDtos, builder.build());
    }

    public MissionRepresentation toMissionRepresentation() {
        Map<String, Block> blockMap = blocks.stream().map(BlockDto::toBlock).collect(Collectors.toMap(Block::id, identity()));

        ImmutableMissionRepresentation.Builder builder = ImmutableMissionRepresentation.builder(blockMap.get(rootBlockId));
        for (Block block : blockMap.values()) {
            List<String> childrenIds = blockChildrenIds.get(block.id());
            if (childrenIds != null) {
                childrenIds.stream()
                        .map(blockMap::get)
                        .forEach(child -> builder.parentToChild(block, child));
            }
        }
        return builder.build();
    }


}
