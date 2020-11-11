package io.molr.mole.core.runnable.lang;

import java.text.MessageFormat;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import io.molr.commons.domain.ExecutionStrategy;

public class ExecutionStrategyConfiguration {
	
	private final ExecutionStrategy defaultStrategy;
	private final Set<ExecutionStrategy> allowedStrategies;
	
	
	private ExecutionStrategyConfiguration() {
		allowedStrategies = ImmutableSet.copyOf(ExecutionStrategy.values());
		defaultStrategy = ExecutionStrategy.PAUSE_ON_ERROR;
	}
	
	private ExecutionStrategyConfiguration(ExecutionStrategy defaultStrategy, Set<ExecutionStrategy> allowedStrategies) {
		this.allowedStrategies = ImmutableSet.copyOf(allowedStrategies);
		this.defaultStrategy = defaultStrategy;
	}
	
	public ExecutionStrategy defaultStrategy() {
		return defaultStrategy;
	}

	public Set<ExecutionStrategy> allowedStrategies() {
		return allowedStrategies;
	}

	@Override
	public String toString() {
		return MessageFormat.format("{defaultStrategy={0}, allowedStrategies={1}}",defaultStrategy, allowedStrategies);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allowedStrategies == null) ? 0 : allowedStrategies.hashCode());
		result = prime * result + ((defaultStrategy == null) ? 0 : defaultStrategy.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExecutionStrategyConfiguration other = (ExecutionStrategyConfiguration) obj;
		if (allowedStrategies == null) {
			if (other.allowedStrategies != null)
				return false;
		} else if (!allowedStrategies.equals(other.allowedStrategies))
			return false;
		if (defaultStrategy != other.defaultStrategy)
			return false;
		return true;
	}



	public static class Builder {

		private ExecutionStrategy defaultStrategy = null;
		private Set<ExecutionStrategy> allowedStrategies = null;
		
		public static Builder builder() {
			return new Builder();
		}
		
		public Builder defaultsTo(ExecutionStrategy executionStrategy) {
			if(this.defaultStrategy!=null) throw new IllegalStateException("defaultsTo must not be called twice.");
			defaultStrategy = executionStrategy;
			return this;
		}

		public Builder allowedStrategies(Set<ExecutionStrategy> allowedStrategies) {
			if(this.allowedStrategies!=null) throw new IllegalStateException("allowedStrategies must not be called twice.");
			this.allowedStrategies = allowedStrategies;
			return this;
		}
		
		ExecutionStrategyConfiguration build() {
			if(allowedStrategies == null) {
				if(defaultStrategy == null) {
					return new ExecutionStrategyConfiguration();
				}
				else {
					return new ExecutionStrategyConfiguration(defaultStrategy, ImmutableSet.of(defaultStrategy));
				}
			}
			else {
				if(allowedStrategies.size()==0) {
					throw new IllegalArgumentException("At least one Strategy has to be specified as allowed execution strategy.");
				}
				if(defaultStrategy == null) {
					if(allowedStrategies.size()>1) {
						throw new IllegalArgumentException("Default execution strategy is missing.");
					}
					return new ExecutionStrategyConfiguration(allowedStrategies.iterator().next(), allowedStrategies);	
				}
				else {
					if(allowedStrategies.contains(defaultStrategy)) {
						return new ExecutionStrategyConfiguration(defaultStrategy, allowedStrategies);
					}
					throw new IllegalArgumentException(MessageFormat.format("Default strategy {0} is not a member of {1}", defaultStrategy, allowedStrategies));
				}
			}

		}
		
	}
	
	

}
