package io.molr.mole.core.runnable.lang;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import io.molr.commons.domain.Block;
import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;
import io.molr.commons.domain.Placeholders;
import io.molr.mole.core.runnable.RunnableLeafsMission.Builder;
import io.molr.mole.core.runnable.lang.ctx.OngoingContextualBranch;

public class ForeachBranchRootMapped<T, U> extends GenericOngoingBranch<ForeachBranchRootMapped<T, U>> {

    /* XXX why can this not be final? */
    private Block block;

    private final Placeholder<T> itemPlaceholder;
    private final Placeholder<U> transformedItemPlaceholder;
    private final Placeholder<? extends Collection<T>> itemsPlaceholder;
    private final Function<In, U> function;

    public ForeachBranchRootMapped(BlockNameConfiguration name, Builder builder, Block parent, BranchMode mode,
            Placeholder<? extends Collection<T>> itemsPlaceholder, Placeholder<T> itemPlaceholder,
            Function<In, U> function, Map<Placeholder<?>, Function<In, ?>> mappings) {
        super(name, builder, parent, mode, mappings);

        this.itemsPlaceholder = requireNonNull(itemsPlaceholder);
        this.itemPlaceholder = requireNonNull(itemPlaceholder);
        this.transformedItemPlaceholder = (Placeholder<U>) Placeholder.of(Object.class, UUID.randomUUID().toString());
        this.function = requireNonNull(function);
    }

    private void createAndAddForeachBlock() {
        this.block = block();
        builder().forEachBlock(block, itemsPlaceholder, itemPlaceholder, transformedItemPlaceholder, function);
    }

    public OngoingContextualBranch<U> branch(String name, Placeholder<?>... placeholders) {
        createAndAddForeachBlock();
        for (int i = 0; i < placeholders.length; i++) {
            if (placeholders[i].equals(Placeholders.LATEST_FOREACH_ITEM_PLACEHOLDER)) {
                placeholders[i] = itemPlaceholder;
            }
        }
        BlockNameConfiguration blockNameConfig = BlockNameConfiguration.builder().text(name)
                .formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build();
        return new OngoingContextualBranch<>(blockNameConfig, builder(), block, BranchMode.sequential(),
                transformedItemPlaceholder, getMappings());
    }

    public OngoingForeachLeaf<U> leaf(String name, Placeholder<?>... placeholders) {
        createAndAddForeachBlock();
        BlockNameConfiguration blockNameConfig = BlockNameConfiguration.builder().text(name)
                .formatterPlaceholders(placeholders).foreachItemPlaceholder(itemPlaceholder).build();
        return new OngoingForeachLeaf<>(blockNameConfig, builder(), block, transformedItemPlaceholder, getMappings());
    }

}
