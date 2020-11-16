package io.molr.mole.core.runnable;

import java.util.Collection;
import java.util.function.Function;
import static java.util.Objects.requireNonNull;

import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;

public class ForEachConfiguration<T,U> {
    
    private final Placeholder<? extends Collection<T>> collectionPlaceholder;
    
    private final Placeholder<T> itemPlaceholder;
    
    private final Placeholder<U> transformedItemPlaceholder;
    
    private final Function<In, U> function;
    
    public ForEachConfiguration(Placeholder<? extends Collection<T>> collectionPlaceholder, Placeholder<T> itemPLaceholder, Placeholder<U> transformedItemPlaceholder, Function<In, U> function) {
    	requireNonNull(collectionPlaceholder);
    	this.collectionPlaceholder = collectionPlaceholder;
    	requireNonNull(itemPLaceholder);
        this.itemPlaceholder = itemPLaceholder;
        requireNonNull(transformedItemPlaceholder);
        this.transformedItemPlaceholder = transformedItemPlaceholder;
        requireNonNull(function);
        this.function = function;
    }

    public Placeholder<? extends Collection<T>> collectionPlaceholder() {
        return this.collectionPlaceholder;
    }

    public Placeholder<T> itemPlaceholder() {
        return this.itemPlaceholder;
    }

    public Function<In, U> function() {
        return this.function;
    }

	public Placeholder<U> transformedItemPlaceholder() {
		return transformedItemPlaceholder;
	}
    
    
}

