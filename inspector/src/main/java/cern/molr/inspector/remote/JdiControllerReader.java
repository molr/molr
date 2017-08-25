package cern.molr.inspector.remote;

import cern.molr.inspector.controller.JdiController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;

/**
 * Reads commands from a {@link java.io.BufferedReader} and proxies them to a given
 * {@link JdiController}. The reader runs a separate thread pool to continuously read
 * input from the stream.
 */
public class JdiControllerReader extends RemoteReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdiControllerReader.class);

    private final JdiController controller;

    /**
     * Creates a reader which reads commands from the given reader and forwards them to the controller.
     *
     * @param reader     The reader to read incoming commands from.
     * @param controller The controller to relay commands to.
     */
    public JdiControllerReader(BufferedReader reader, JdiController controller) {
        super(reader);
        this.controller = controller;
    }

    /**
     * Creates a reader which reads commands from the given reader and forwards them to the controller.
     *
     * @param reader       The reader to read incoming commands from.
     * @param controller   The controller to relay commands to.
     * @param readInterval The interval with which commands should be read. Cannot be negative.
     */
    public JdiControllerReader(BufferedReader reader, JdiController controller, Duration readInterval) {
        super(reader, readInterval);
        this.controller = controller;
    }

    private static void closeResource(AutoCloseable closeable, String error) {
        try {
            closeable.close();
        } catch (Exception e) {
            LOGGER.warn(error, e);
        }
    }

    private void forwardCommand(JdiControllerCommand command) {
        switch (command) {
            case STEP_FORWARD:
                controller.stepForward();
                break;
            case TERMINATE:
                controller.terminate();
                break;
            case RESUME:
                controller.resume();
                break;
        }
    }

    protected void readCommand(BufferedReader reader) {
        try {
            int code = Character.getNumericValue(reader.read());
            if (code != -1) {
                JdiControllerCommand[] values = JdiControllerCommand.values();
                if (code >= 0 && code < values.length) {
                    forwardCommand(values[code]);
                } else {
                    LOGGER.error("Received illegal command {}, expected a number between 0 and {}", code, values.length - 1);
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to read command from reader: ", e);
        }
    }

}
