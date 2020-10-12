package io.molr.mole.core.runnable.lang;

import java.util.UUID;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;

public class ForeachBranchRoot<T> extends GenericOngoingBranch<ForeachBranchRoot<T>>{

	private Block block;
	Placeholder<T> itemPlaceholder;
	
	public ForeachBranchRoot(String name, Builder builder, Block parent, BranchMode mode, Placeholder<? extends MolrCollection<T>> itemsPlaceholder) {
		super(name, builder, parent, mode);

       this.itemPlaceholder = MolrCollection.itemPlaceholderForCollectionPlaceholder(itemsPlaceholder, UUID.randomUUID().toString());
        System.out.println(itemPlaceholder);


       this.block = block();
        builder().forEachBlock(block, itemsPlaceholder, itemPlaceholder);
	}

	public OngoingForeachBranch<T> branch(String name) {
		return new OngoingForeachBranch<>(name, builder(), block, mode(), itemPlaceholder);
	}
	
	public OngoingForeachLeaf leaf(String name) {
		return new OngoingForeachLeaf(name, builder(), block);
	}

}
