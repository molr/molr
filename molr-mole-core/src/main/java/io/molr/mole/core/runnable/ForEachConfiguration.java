package io.molr.mole.core.runnable;

import java.util.function.BiConsumer;

import io.molr.commons.domain.In;
import io.molr.commons.domain.Out;
import io.molr.commons.domain.Placeholder;

public class ForEachConfiguration<T,U> {
    
    private final Placeholder<T> collectionPlaceholder;
    
    private final Placeholder<U> itemPlaceholder;
    
    private final BiConsumer<In, Out> runnable;
    
    public ForEachConfiguration(Placeholder<T> collectionPlaceholder, Placeholder<U> itemPLaceholder, BiConsumer<In, Out> runnable) {
        this.collectionPlaceholder = collectionPlaceholder;
        this.itemPlaceholder = itemPLaceholder;
        this.runnable = runnable;
    }

    public Placeholder<T> collectionPlaceholder() {
        return this.collectionPlaceholder;
    }

    public Placeholder<U> itemPlaceholder() {
        return this.itemPlaceholder;
    }

    public BiConsumer<In, Out> runnable() {
        return this.runnable;
    }
    
}

