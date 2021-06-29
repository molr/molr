package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission;

import static io.molr.mole.core.runnable.lang.BranchMode.PARALLEL;
import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Function;

public abstract class GenericOngoingBranch<B extends GenericOngoingBranch<B>> extends OngoingNode<B> {

    private BranchMode mode;
    
    public GenericOngoingBranch(BlockNameConfiguration name, RunnableLeafsMission.Builder builder, Block parent, BranchMode mode, Map<Placeholder<?>, Function<In, ?>> mappings) {
        super(
                requireNonNull(name, "branchName must not be null"),
                requireNonNull(builder, "builder must not be null"),
                parent, mappings /* parent may be null (special case for root branch)*/
        );
        this.mode = requireNonNull(mode);
    }

    public B parallel() {
        this.mode = PARALLEL;
        return (B) this;
    }
    
    public B parallel(int maxConcurrency) {
        this.mode = BranchMode.newParallel(maxConcurrency);
        return (B) this;
    }

    public B sequential() {
        this.mode = SEQUENTIAL;
        return (B) this;
    }

    protected Block block() {
        Block block;
    	if (parent() == null) {
            block = builder().rootBranchNode(name(), mode, blockAttributes());
        } else {
            block = builder().childBranchNode(parent(), name(), mode, blockAttributes());
        }
    	/*
    	 * TODO find another place for this method
    	 */
        if(!getMappings().isEmpty()) {
            builder().addBlockLetValues(block, getMappings());
        }

        return block;
    }

    protected BranchMode mode() {
        return this.mode;
    }

}
