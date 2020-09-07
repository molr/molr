package io.molr.commons.domain;

import java.util.HashMap;
import java.util.Map;

public enum ExecutionStrategy {

    PAUSE_ON_ERROR, PROCEED_ON_ERROR, ABORT_ON_ERROR;
    
    private static final Map<String, ExecutionStrategy> namesToStrategy = new HashMap<>();
    
    static {
        for(ExecutionStrategy strategy : ExecutionStrategy.values()){
            namesToStrategy.put(strategy.name(), strategy);
        }
    }
    
    public static ExecutionStrategy forName(String strategyName) {
        return namesToStrategy.get(strategyName);
    }
    
}

