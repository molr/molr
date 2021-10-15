package io.molr.mole.core.runnable.lang;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.ctx.OngoingContextualBranch;

public class ForeachBranchRoot<T> extends GenericOngoingBranch<ForeachBranchRoot<T>> {

	private Block block;
	Placeholder<T> itemPlaceholder;
	Placeholder<? extends Collection<T>> itemsPlaceholder;

	@SuppressWarnings("unchecked")
	public ForeachBranchRoot(BlockNameConfiguration name, Builder builder, Block parent, BranchMode mode,
			Placeholder<? extends Collection<T>> itemsPlaceholder, Map<Placeholder<?>, Function<In, ?>> mappings) {
		super(name, builder, parent, mode, mappings);
		requireNonNull(itemsPlaceholder);
		this.itemsPlaceholder = itemsPlaceholder;
		this.itemPlaceholder = (Placeholder<T>) Placeholder.of(Object.class, UUID.randomUUID().toString());
	}
	
	public ForeachBranchRoot(BlockNameConfiguration name, Builder builder, Block parent, BranchMode mode,
			Placeholder<T> itemPlaceholder,
			Placeholder<? extends Collection<T>> itemsPlaceholder, Map<Placeholder<?>, Function<In, ?>> mappings) {
		super(name, builder, parent, mode, mappings);
		requireNonNull(itemsPlaceholder);
		this.itemsPlaceholder = itemsPlaceholder;
		this.itemPlaceholder = itemPlaceholder;
	}

	private void createAndAddForeachBlock() {
		this.block = block();
		builder().forEachBlock(block, itemsPlaceholder, itemPlaceholder);
	}

	public OngoingContextualBranch<T> branch(String name, Placeholder<?>... placeholders) {
		createAndAddForeachBlock();
		return new OngoingContextualBranch<>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build(),
				builder(), block, BranchMode.sequential(), itemPlaceholder, getMappings());
	}

	public OngoingForeachLeaf<T> leaf(String name,  Placeholder<?>... placeholders) {
		createAndAddForeachBlock();
		return new OngoingForeachLeaf<>(BlockNameConfiguration.builder().text(name).formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build(),
				builder(), block, itemPlaceholder, getMappings());
	}

    public <C> ForeachBranchRootMapped<T, C> map(Function<T, C> contextFactory) {
        return mapIt(in -> contextFactory.apply(in.get(itemPlaceholder)));
    }

    public <C> ForeachBranchRootMapped<T, C> map(BiFunction<T, In, C> contextFactory) {
        return mapIt(in -> contextFactory.apply(in.get(itemPlaceholder), in));
    }

    public <C, P1> ForeachBranchRootMapped<T, C> map(BiFunction<T, P1, C> contextFactory, Placeholder<P1> p1) {
        return mapIt(in -> contextFactory.apply(in.get(itemPlaceholder), in.get(p1)));
    }

    private <C> ForeachBranchRootMapped<T, C> mapIt(Function<In, C> contextFactory) {
        return new ForeachBranchRootMapped<>(name(), builder(), parent(), mode(), itemsPlaceholder, itemPlaceholder,
                contextFactory, getMappings());
    }
}
