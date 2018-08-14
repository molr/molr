package org.molr.commons.api.domain;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class ImmutableMissionRepresentation implements MissionRepresentation {

    private final Mission mission;
    private final Block root;

    public ImmutableMissionRepresentation(Mission mission, Block root) {
        this.mission = mission;
        this.root = root;
    }

    @Override
    public Mission mission() {
        return this.mission;
    }

    @Override
    public Block rootBlock() {
        return this.root;
    }

    @Override
    public List<Block> childrenOf(Block block) {
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImmutableMissionRepresentation that = (ImmutableMissionRepresentation) o;
        return Objects.equals(mission, that.mission) &&
                Objects.equals(root, that.root);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mission, root);
    }


    public static class Builder {

        private final AtomicLong idSeq = new AtomicLong(0);

        private BlockId newId() {
            return new BlockId("" + idSeq.getAndIncrement());
        }

    }


    public static class BlockId {
        private final String id;

        private BlockId(String id) {
            this.id = id;
        }
    }
}
