package io.molr.mole.core.runnable.lang.ctx;

import java.util.UUID;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.MolrCollection;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.BranchMode;
import io.molr.mole.core.runnable.lang.GenericOngoingBranch;

public class ContextualForeachBranchRoot<C, T> extends GenericOngoingBranch<ContextualForeachBranchRoot<C, T>> {

	private Block block;
	Placeholder<T> itemPlaceholder;
	Placeholder<C> contextPlaceholder;
	Placeholder<? extends MolrCollection<T>> itemsPlaceholder;

	public ContextualForeachBranchRoot(String name, Builder builder, Block parent, BranchMode mode, Placeholder<C> contextPlaceholder,
			Placeholder<? extends MolrCollection<T>> itemsPlaceholder) {
		super(name, builder, parent, mode);

		this.contextPlaceholder = contextPlaceholder;
		this.itemsPlaceholder = itemsPlaceholder;
		this.itemPlaceholder = MolrCollection.itemPlaceholderForCollectionPlaceholder(itemsPlaceholder,
				UUID.randomUUID().toString());
	}

	private void createAndAddForeachBlock() {
		this.block = block();
		builder().forEachBlock(block, itemsPlaceholder, itemPlaceholder);
	}
	
	public ContextualOngoingForeachBranchRoot<C, T> branch(String name) {
		createAndAddForeachBlock();
		return new ContextualOngoingForeachBranchRoot<>(name, builder(), block, BranchMode.SEQUENTIAL, contextPlaceholder, itemPlaceholder);
	}

	public ContextualOngoingForeachLeaf<C, T> leaf(String name) {
		createAndAddForeachBlock();
		return new ContextualOngoingForeachLeaf<C, T>(name, builder(), block, contextPlaceholder, itemPlaceholder);
	}

}
