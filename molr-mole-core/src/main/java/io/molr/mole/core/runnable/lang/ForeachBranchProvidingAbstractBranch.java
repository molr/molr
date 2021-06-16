package io.molr.mole.core.runnable.lang;

import static io.molr.mole.core.runnable.lang.BranchMode.SEQUENTIAL;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.ContextConfiguration;
import io.molr.mole.core.runnable.ForEachConfiguration;
import io.molr.mole.core.runnable.RunnableLeafsMission;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;

public abstract class ForeachBranchProvidingAbstractBranch extends AbstractBranch{

	protected ForeachBranchProvidingAbstractBranch(Builder builder, Block parent) {
		super(builder, parent);

	}
	
    public <T> ForeachBranchRoot<T> foreach(Placeholder<? extends MolrCollection<T>> itemsPlaceholder) {
    	requireNonNull(itemsPlaceholder);
    	String name = "forEachItemIn:"+itemsPlaceholder.name();
    	BlockNameConfiguration formatter = BlockNameConfiguration.builder().text(name).build();
        return new ForeachBranchRoot<>(formatter, builder(), parent(), BranchMode.SEQUENTIAL, itemsPlaceholder);
    }
    
}
