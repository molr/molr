package io.molr.mole.core.logging.consumer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO Add javadoc
public class LogConsumerFactory {
    private static Map<Class<?>, LogConsumer> logConsumerMap = new ConcurrentHashMap<>();

    public static <T> LogConsumer consumer(Class<T> clz) throws IllegalAccessException, InstantiationException {
        synchronized (logConsumerMap) {
            if (!logConsumerMap.containsKey(clz)) {
                LogConsumer logConsumer = (LogConsumer) clz.newInstance();
                logConsumerMap.put(clz, logConsumer);
            }
            return logConsumerMap.get(clz);
        }
    }
}
