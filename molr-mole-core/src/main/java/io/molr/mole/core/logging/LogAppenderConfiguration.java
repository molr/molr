package io.molr.mole.core.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.molr.mole.core.logging.consumer.LogConsumer;
import io.molr.mole.core.logging.consumer.LogConsumerFactory;
import io.molr.mole.core.logging.data.LogData;
import io.molr.mole.core.logging.filter.RunningMissionFilter;
import io.molr.mole.core.logging.publisher.LogPublisher;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

// TODO Add javadoc
public class LogAppenderConfiguration {

    private final static String propertiesFile = "logAppender.properties";

    public LogAppenderConfiguration() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        configureAppender(getLogProperties());
    }

    private Properties getLogProperties() throws IOException {
        Properties properties = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream resourceStream = loader.getResourceAsStream(propertiesFile);
        properties.load(resourceStream);
        return properties;
    }

    private void configureAppender(Properties properties) throws IllegalAccessException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        String appenders = properties.getProperty("log.appender");
        if (StringUtils.isEmpty(appenders)) {
            return;
        }

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = (Logger) context.getLogger(Logger.ROOT_LOGGER_NAME);

        List<String> appenderNames = Arrays.asList(appenders.split(","));
        for (String appenderName : appenderNames) {
            loadAppender(context, rootLogger, properties, appenderName);
        }
    }

    private void loadAppender(LoggerContext context, Logger rootLogger, Properties properties, String appenderName) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        String appenderClassName = properties.getProperty("log.appender." + appenderName);
        String dataClassName = properties.getProperty("log.appender." + appenderName + ".data");
        String publisherClassName = properties.getProperty("log.appender." + appenderName + ".publisher");
        if (StringUtils.isEmpty(appenderClassName) || StringUtils.isEmpty(dataClassName) || StringUtils.isEmpty(publisherClassName)) {
            return;
        }

        LogData data = (LogData) Class.forName(dataClassName).newInstance();
        LogPublisher publisher = (LogPublisher) Class.forName(publisherClassName).newInstance();
        Appender<ILoggingEvent> appender = (Appender<ILoggingEvent>) Class.forName(appenderClassName).getConstructor(LogData.class, LogPublisher.class).newInstance(data, publisher);

        addConsumers(properties, appenderName, publisher);

        addFilters(properties, appenderName, appender);

        appender.setName(appenderName);
        appender.setContext(context);
        appender.start();

        rootLogger.addAppender(appender);
    }

    private void addFilters(Properties properties, String appenderName, Appender<ILoggingEvent> appender) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String filters = properties.getProperty("log.appender." + appenderName + ".filter");
        if (StringUtils.isEmpty(filters)) {
            return;
        }

        List<String> filterNames = Arrays.asList(filters.split(","));
        for (String filterName : filterNames) {
            String filterClassName = properties.getProperty("log.appender." + appenderName + ".filter." + filterName);
            if (!StringUtils.isEmpty(filterClassName)) {
                RunningMissionFilter filter = (RunningMissionFilter) Class.forName(filterClassName).newInstance();
                appender.addFilter(filter);
            }
        }
    }

    private void addConsumers(Properties properties, String appenderName, LogPublisher publisher) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String consumers = properties.getProperty("log.appender." + appenderName + ".consumer");
        if (StringUtils.isEmpty(consumers)) {
            return;
        }
        List<String> consumerNames = Arrays.asList(consumers.split(","));
        for (String consumerName : consumerNames) {
            String consumerClassName = properties.getProperty("log.appender." + appenderName + ".consumer." + consumerName);
            if (!StringUtils.isEmpty(consumerClassName)) {
                LogConsumer consumer = LogConsumerFactory.consumer(Class.forName(consumerClassName));
                publisher.addConsumer(consumer);
            }
        }
    }
}
