package io.molr.mole.core.runnable;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import io.molr.commons.domain.In;
import io.molr.commons.domain.Placeholder;

public class ContextConfiguration {

	private final Function<In, ?> contextFactory;
	private final Placeholder<?> contextPlaceholder;
	
	public ContextConfiguration(Function<In, ?> contextFactory, Placeholder<?> contextPlaceholder) {
		requireNonNull(contextFactory);
		requireNonNull(contextPlaceholder);
		this.contextFactory = contextFactory;
		this.contextPlaceholder = contextPlaceholder;
	}

	public Function<In, ?> contextFactory() {
		return contextFactory;
	}

	public Placeholder<?> contextPlaceholder() {
		return contextPlaceholder;
	}

}
