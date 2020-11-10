package io.molr.mole.core.runnable.lang;

import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;

import io.molr.commons.domain.ExecutionStrategy;

public class ExecutionStrategyConfiguration {
	
	private ExecutionStrategy defaultStrategy = ExecutionStrategy.PAUSE_ON_ERROR;
	private Set<ExecutionStrategy> allowedStrategies = Sets.immutableEnumSet(ExecutionStrategy.PAUSE_ON_ERROR);
	
	/**
	 * Sets the default execution strategy for this mission.
	 * @param executionStrategy
	 * @return
	 */
	public ExecutionStrategyConfiguration defaultsTo(ExecutionStrategy executionStrategy) {
		defaultStrategy = executionStrategy;
		return this;
	}
	
	/**
	 * Sets the allowed strategies the mission can be configured with.
	 * @param strategies
	 * @return
	 */
	public ExecutionStrategyConfiguration allowed(ExecutionStrategy... strategies) {
		allowedStrategies = Sets.immutableEnumSet(defaultStrategy, strategies);
		return this;
	}
	
	Set<String> allowedStrategies(){
		return this.allowedStrategies.stream().map(ExecutionStrategy::name).collect(Collectors.toSet());
	}
	
	String defaultStrategy() {
		return this.defaultStrategy.name();
	}

}
