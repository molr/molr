package cern.molr.inspector.entry;

import cern.molr.inspector.entry.impl.EntryStateImpl;
import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by mgalilee on 17/02/2016.
 */
public class EntryStateBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryStateBuilder.class);

    public static Optional<EntryState> ofLocation(Location location) {
        try {
            final String className = location.sourcePath();
            final String methodName = location.method().name();
            final EntryState entryState = new EntryStateImpl(className, methodName, location.lineNumber());
            return Optional.of(entryState);
        } catch (AbsentInformationException e) {
            LOGGER.warn("Failed to get entry state from thread state: missing source name of thread class", e);
            return Optional.empty();
        }
    }
}
