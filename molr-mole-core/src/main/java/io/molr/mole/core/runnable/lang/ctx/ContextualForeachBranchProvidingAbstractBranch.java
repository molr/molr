package io.molr.mole.core.runnable.lang.ctx;

import java.util.Collection;

import io.molr.commons.domain.Block;

import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.AbstractBranch;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.BlockNameConfiguration;

public abstract class ContextualForeachBranchProvidingAbstractBranch<C> extends AbstractBranch{

	private Placeholder<C> contextPlaceholder;

	protected ContextualForeachBranchProvidingAbstractBranch(Builder builder, Block parent, Placeholder<C> contextPlaceholder) {
		super(builder, parent);
		this.contextPlaceholder = contextPlaceholder;
	}
	
    public <T> ContextualForeachBranchRoot<C, T> foreach(Placeholder<? extends Collection<T>> itemsPlaceholder/*, String name*/) {
    	String name = "forEachItemIn:"+itemsPlaceholder.name();
        return new ContextualForeachBranchRoot<>(BlockNameConfiguration.builder().text(name).build(), builder(), parent(), BranchMode.SEQUENTIAL, contextPlaceholder, itemsPlaceholder);
    }

}
