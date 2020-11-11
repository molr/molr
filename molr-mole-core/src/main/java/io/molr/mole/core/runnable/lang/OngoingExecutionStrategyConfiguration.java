package io.molr.mole.core.runnable.lang;

import java.util.Set;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.ExecutionStrategy;

public class OngoingExecutionStrategyConfiguration {

	
	private final ExecutionStrategyConfiguration.Builder builder;
	
	OngoingExecutionStrategyConfiguration(ExecutionStrategyConfiguration.Builder builder) {
		requireNonNull(builder);
		this.builder = builder;
	}
	
	/**
	 * Sets the default {@link ExecutionStrategy} for this mission. If none default {@link ExecutionStrategy} is specified molr will either use the global default
	 * or the one and only allowed strategy if and only if exactly one strategy has been specified by {@link #allowed}. If multiple allowed strategies have been specified
	 * the default strategy must be a member of the allowed strategies.
	 *
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
	
	/**
	 * Allow for all strategies to be used with this mission.
	 * @return
	 */
	public OngoingExecutionStrategyConfiguration allowAll() {
		Set<ExecutionStrategy> allowedStrategies = ImmutableSet.copyOf(ExecutionStrategy.values());
		builder.allowedStrategies(allowedStrategies);
		return this;
	}
}
