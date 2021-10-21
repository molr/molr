package io.molr.mole.core.runnable.lang;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.ExecutionStrategy;

@SuppressWarnings("static-method")
public class ExecutionStrategyConfigurationTest {
	
	@Test
	public void build_whenDefaultStrategyAndAllowedIsProvided_configurationAsSpecified() {
		ExecutionStrategyConfiguration configuration = ExecutionStrategyConfiguration.Builder.builder().defaultsTo(ExecutionStrategy.ABORT_ON_ERROR).allowedStrategies(ImmutableSet.of(ExecutionStrategy.ABORT_ON_ERROR, ExecutionStrategy.PAUSE_ON_ERROR)).build();
		Assertions.assertThat(configuration.defaultStrategy()).isEqualTo(ExecutionStrategy.ABORT_ON_ERROR);
		Assertions.assertThat(configuration.allowedStrategies()).containsAll(ImmutableSet.of(ExecutionStrategy.ABORT_ON_ERROR, ExecutionStrategy.PAUSE_ON_ERROR));
	}
	
	@Test
	public void build_whenDefaultStrategyIsProvidedAndAllowedIsNull_configurationAsSpecified() {
		ExecutionStrategyConfiguration configuration = ExecutionStrategyConfiguration.Builder.builder().defaultsTo(ExecutionStrategy.ABORT_ON_ERROR).build();
		Assertions.assertThat(configuration.defaultStrategy()).isEqualTo(ExecutionStrategy.ABORT_ON_ERROR);
		Assertions.assertThat(configuration.allowedStrategies()).containsAll(ImmutableSet.of(ExecutionStrategy.ABORT_ON_ERROR));
	}
	
	@Test
	public void build_whenDefaultStrategyIsMissingAndAllowedStrategiesMissing_defaultStrategyIsPAUSE_ON_ERRORandAllowedValuesContainsAll() {
		ExecutionStrategyConfiguration configuration = ExecutionStrategyConfiguration.Builder.builder().build();
		Assertions.assertThat(configuration.defaultStrategy()).isEqualTo(ExecutionStrategy.PAUSE_ON_ERROR);
		Assertions.assertThat(configuration.allowedStrategies()).containsExactlyInAnyOrder(ExecutionStrategy.values());
	}
	
	@Test
	public void build_whenDefaultStrategyIsMissingButAllowedStrategiesSizeIs1_defaultStrategyEqualsItemInAllowedValues() {
		ExecutionStrategyConfiguration configuration = ExecutionStrategyConfiguration.Builder.builder().allowedStrategies(ImmutableSet.of(ExecutionStrategy.ABORT_ON_ERROR)).build();
		Assertions.assertThat(configuration.defaultStrategy()).isEqualTo(ExecutionStrategy.ABORT_ON_ERROR);
		Assertions.assertThat(configuration.allowedStrategies()).containsExactlyInAnyOrder(ExecutionStrategy.ABORT_ON_ERROR);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void build_whenAllowedStrategiesIsEmptyList_illegalArgumentExceptionIsThrown() {
		ExecutionStrategyConfiguration.Builder.builder().allowedStrategies(ImmutableSet.of()).defaultsTo(ExecutionStrategy.PAUSE_ON_ERROR).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void build_whenDefaultStrategyIsMissing_illegalArgumentExceptionIsThrown() {
		ExecutionStrategyConfiguration.Builder.builder().allowedStrategies(ImmutableSet.copyOf(ExecutionStrategy.values())).build();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void build_whenDefaultStrategyIsPresentButMissingInAllowedStrategies_illegalArgumentExceptionIsThrown() {
		ExecutionStrategyConfiguration.Builder.builder().allowedStrategies(ImmutableSet.of(ExecutionStrategy.ABORT_ON_ERROR)).defaultsTo(ExecutionStrategy.PAUSE_ON_ERROR).build();
	}
	
	@Test(expected = IllegalStateException.class)
	public void build_whenDefaultsToIsCalledTwice_illegalArgumentExceptionIsThrown() {
		ExecutionStrategyConfiguration.Builder.builder().defaultsTo(ExecutionStrategy.PAUSE_ON_ERROR).defaultsTo(ExecutionStrategy.PAUSE_ON_ERROR).build();
	}
	
	
	@Test(expected = IllegalStateException.class)
	public void build_whenAllowedStrategiesIsCalledTwice_illegalArgumentExceptionIsThrown() {
		ExecutionStrategyConfiguration.Builder.builder().allowedStrategies(ImmutableSet.of(ExecutionStrategy.ABORT_ON_ERROR)).allowedStrategies(ImmutableSet.of(ExecutionStrategy.ABORT_ON_ERROR)).build();
	}

}
