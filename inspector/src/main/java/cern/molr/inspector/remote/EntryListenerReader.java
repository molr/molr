package cern.molr.inspector.remote;

import cern.molr.inspector.entry.EntryListener;
import cern.molr.inspector.entry.EntryState;
import cern.molr.inspector.entry.impl.EntryStateImpl;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

/**
 * A reader which continually listens for commands from a {@link java.io.BufferedReader}.
 */
public class EntryListenerReader extends RemoteReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryListenerReader.class);

    private final Gson gson = new Gson();
    private final EntryListener listener;

    /**
     * Instructs the reader to receive commands from the given {@link BufferedReader} and forward them to a
     * {@link EntryListener}.
     *
     * @param reader   The reader to read commands from.
     * @param listener The listener that should receive the commands.
     */
    public EntryListenerReader(BufferedReader reader, EntryListener listener) {
        super(reader);
        this.listener = listener;
    }

    /**
     * Instructs the reader to receive commands from the given {@link BufferedReader} and forward them to a
     * {@link EntryListener}.
     *
     * @param reader       The reader to read commands from.
     * @param listener     The listener that should receive the commands.
     * @param readInterval The time interval between checks for new input.
     */
    public EntryListenerReader(BufferedReader reader, EntryListener listener, Duration readInterval) {
        super(reader, readInterval);
        this.listener = listener;
    }

    private void readCommand(EntryListenerMethod method, BufferedReader reader) throws IOException {
        LOGGER.debug(method.name());
        switch (method) {
            case ON_VM_DEATH:
                listener.onVmDeath();
                close();
                break;
            case ON_LOCATION_CHANGE:
                readState(reader).ifPresent(listener::onLocationChange);
                break;
            case ON_INSPECTION_END:
                readState(reader).ifPresent(listener::onInspectionEnd);
                break;
        }
    }

    @Override
    protected void readCommand(BufferedReader reader) {
        try {
            Optional<EntryListenerMethod> method = readMethod(reader);
            if (method.isPresent()) {
                readCommand(method.get(), reader);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read call to entry listener: ", e);
        }
    }

    private Optional<EntryListenerMethod> readMethod(BufferedReader reader) throws IOException {
        final String line = reader.readLine();
        if (line != null) {
            if (line.length() != 1) {
                LOGGER.warn("Expected numeric character, but received {}", line);
            } else {
                int method = Character.getNumericValue(line.charAt(0));
                EntryListenerMethod[] methods = EntryListenerMethod.values();
                if (method != -1) {
                    if (method >= 0 && method < methods.length) {
                        return Optional.of(methods[method]);
                    } else {
                        LOGGER.warn("Received illegal command {}, expected a number between 0 and {}", method, methods.length);
                    }
                }
            }
        } else {
            close();
        }
        return Optional.empty();
    }

    private Optional<EntryState> readState(BufferedReader reader) throws IOException {
        final String line = reader.readLine();
        if (line != null) {
            if (line.isEmpty()) {
                LOGGER.warn("Expected an EntryState, but got {}", line);
            } else {
                try {
                    EntryStateImpl entryState = gson.fromJson(line, EntryStateImpl.class);
                    LOGGER.debug("no EntryState", entryState);
                    return Optional.of(entryState);
                } catch (JsonSyntaxException e) {
                    LOGGER.warn("Error when parsing json", e);
                }
            }
        }
        LOGGER.debug("no EntryState");
        return Optional.empty();
    }

}
