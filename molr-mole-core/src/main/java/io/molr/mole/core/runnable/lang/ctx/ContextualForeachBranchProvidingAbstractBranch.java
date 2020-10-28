package io.molr.mole.core.runnable.lang.ctx;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.AbstractBranch;
import io.molr.mole.core.runnable.lang.BranchMode;

public abstract class ContextualForeachBranchProvidingAbstractBranch<C> extends AbstractBranch{

	protected ContextualForeachBranchProvidingAbstractBranch(Builder builder, Block parent) {
		super(builder, parent);
		// TODO Auto-generated constructor stub
	}
	
    public <T> ContextualForeachBranchRoot<C, T> foreach(Placeholder<? extends MolrCollection<T>> itemsPlaceholder/*, String name*/) {
    	String name = "forEachItemIn:"+itemsPlaceholder.name();
        return new ContextualForeachBranchRoot<>(name, builder(), parent(), BranchMode.SEQUENTIAL, itemsPlaceholder);
    }


}
