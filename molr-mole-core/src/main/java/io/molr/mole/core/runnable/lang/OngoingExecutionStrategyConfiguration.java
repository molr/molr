package io.molr.mole.core.runnable.lang;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.ExecutionStrategy;

public class OngoingExecutionStrategyConfiguration {

	
	ExecutionStrategyConfiguration.Builder builder;
	
	OngoingExecutionStrategyConfiguration(ExecutionStrategyConfiguration.Builder builder) {
		this.builder = builder;
	}
	
	/**
	 * Sets the default execution strategy for this mission.
	 * @param executionStrategy
	 * @return
	 */
	public OngoingExecutionStrategyConfiguration defaultsTo(ExecutionStrategy executionStrategy) {
		builder.defaultsTo(executionStrategy);
		return this;
	}
	
	/**
	 * Sets the allowed strategies the mission can be configured with.
	 * @param strategies
	 * @return
	 */
	public OngoingExecutionStrategyConfiguration allowed(ExecutionStrategy... strategies) {
		Set<ExecutionStrategy> allowedStrategies = ImmutableSet.copyOf(strategies);
		builder.allowedStrategies(allowedStrategies);
		return this;
	}
}
