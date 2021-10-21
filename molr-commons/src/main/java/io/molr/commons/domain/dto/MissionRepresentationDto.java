package io.molr.commons.domain.dto;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimaps;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.BlockAttribute;
import io.molr.commons.domain.ImmutableMissionRepresentation;
import io.molr.commons.domain.MissionRepresentation;

public class MissionRepresentationDto {

    public final String rootBlockId;
    public final Set<BlockDto> blocks;
    public final Map<String, List<String>> childrenBlockIds;
    public final Map<String, List<BlockAttribute>> blockAttributes;
    

    public MissionRepresentationDto(String rootBlockId, Set<BlockDto> blocks, Map<String, List<String>> childrenBlockIds, Map<String, List<BlockAttribute>> blockAttributes) {
        this.rootBlockId = rootBlockId;
        this.blocks = blocks;
        this.childrenBlockIds = childrenBlockIds;
        this.blockAttributes = blockAttributes;
    }

    public MissionRepresentationDto() {
        this.rootBlockId = null;
        this.blocks = Collections.emptySet();
        this.childrenBlockIds = Collections.emptyMap();
        this.blockAttributes = Collections.emptyMap();
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
        
        ImmutableMap.Builder<String, List<BlockAttribute>> blockAttributes = ImmutableMap.builder();
        Multimaps.asMap(representation.blockAttributes()).forEach((block, attributes)->{
        	blockAttributes.put(block.id(), attributes);
        });
        MissionRepresentationDto dto = new MissionRepresentationDto(representation.rootBlock().id(), blockDtos, builder.build(), blockAttributes.build());
        return dto;
    }

    public MissionRepresentation toMissionRepresentation() {
        Map<String, Block> blockMap = blocks.stream().map(BlockDto::toBlock).collect(Collectors.toMap(Block::id, identity()));
        
        ImmutableMissionRepresentation.Builder builder = ImmutableMissionRepresentation.builder(blockMap.get(rootBlockId));
        for (Block block : blockMap.values()) {
            List<String> childrenIds = childrenBlockIds.get(block.id());
            if (childrenIds != null) {
                childrenIds.stream()
                        .map(blockMap::get)
                        .forEach(child -> builder.parentToChild(block, child));
            }
            
            if(this.blockAttributes.containsKey(block.id())) {
	            List<BlockAttribute> attributes = this.blockAttributes.get(block.id());
	            builder.addBlockAttributes(block, attributes);
            }
            
        }

        return builder.build();
    }


}
