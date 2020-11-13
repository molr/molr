package io.molr.mole.core.runnable.lang;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;

public abstract class ForeachBranchProvidingAbstractBranch extends AbstractBranch{

	protected ForeachBranchProvidingAbstractBranch(Builder builder, Block parent) {
		super(builder, parent);

	}
	
    public <T> ForeachBranchRoot<T> foreach(Placeholder<? extends MolrCollection<T>> itemsPlaceholder) {
    	String name = "forEachItemIn:"+itemsPlaceholder.name();
    	BlockNameConfiguration formatter = BlockNameConfiguration.builder().build();
        return new ForeachBranchRoot<>(formatter, builder(), parent(), BranchMode.SEQUENTIAL, itemsPlaceholder);
    }

}
