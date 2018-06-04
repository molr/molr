package cern.molr.supervisor.impl.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Reads commands from a {@link BufferedReader} and proxies them to a given function in a regular interval. Unless
 * specified, the interval defaults to 100 milliseconds. The reader runs a separate thread to continuously read input
 * from the stream without blocking.
 *
 * @author ?
 * @author yassine-kr
 */
public abstract class RemoteReader implements AutoCloseable {

    protected static final Duration DEFAULT_READING_INTERVAL = Duration.ofMillis(100);
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteReader.class);
    private final ScheduledExecutorService service;
    private final BufferedReader reader;
    private Optional<Runnable> onClose = Optional.empty();
    private Optional<Runnable> onReadingAttempt = Optional.empty();

    private Runnable read;
    private Duration readingInterval;
    private boolean started = false;

    /**
     * Creates a reader which reads commands from the given reader and forwards them to the controller.
     *
     * @param reader The reader to read incoming commands from.
     */
    public RemoteReader(BufferedReader reader) {
        this(reader, DEFAULT_READING_INTERVAL);
    }

    /**
     * Creates a reader which reads commands from the given reader and forwards them to the controller.
     *
     * @param reader          The reader to read incoming commands from.
     * @param readingInterval The interval with which to read commands. May not be negative.
     */
    public RemoteReader(BufferedReader reader, Duration readingInterval) {
        this(reader, readingInterval, readingInterval);
    }

    /**
     * A constructor which define a delay before launching the periodic reader
     *
     * @param reader
     * @param readingInterval
     * @param initialDelay    the time to wait before launching the periodic reader, if it is null,
     *                        the reader is not started, it should be started manually
     */
    public RemoteReader(BufferedReader reader, Duration readingInterval, Duration initialDelay) {
        if (readingInterval.isNegative()) {
            throw new IllegalArgumentException("Reading interval cannot be less than 0");
        }
        this.reader = reader;
        this.readingInterval = readingInterval;
        service = Executors.newSingleThreadScheduledExecutor();
        read = () -> {
            try {
                onReadingAttempt.ifPresent(Runnable::run);
                if (reader.ready()) {
                    readCommand(reader);
                }
            } catch (Exception exception) {
                LOGGER.warn("Exception trying to read command", exception);
            }
        };
        if (initialDelay != null) {
            service.scheduleAtFixedRate(read, initialDelay.toMillis(),
                    readingInterval.toMillis(), TimeUnit.MILLISECONDS);
            started = true;
        }

    }

    static void closeResource(AutoCloseable closeable, Consumer<Exception> onError) {
        try {
            closeable.close();
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    /**
     * Method which allows to manually start the reader
     */
    protected void start() {
        if (!started) {
            service.scheduleAtFixedRate(read, 0,
                    readingInterval.toMillis(), TimeUnit.MILLISECONDS);
            started = true;
        }
    }

    /**
     * Reads a command from the given {@link BufferedReader}.
     *
     * @param reader The reader to read the next command from.
     */
    protected abstract void readCommand(BufferedReader reader);

    @Override
    public void close() {
        closeResource(reader, e -> LOGGER.warn("Failed to close reader", e));
        service.shutdown();
        onClose.ifPresent(Runnable::run);
    }

    public void setOnReadingAttempt(Runnable onReadingAttempt) {
        this.onReadingAttempt = Optional.ofNullable(onReadingAttempt);
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = Optional.of(onClose);
    }

}
