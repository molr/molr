package cern.molr.supervisor.impl.session;

import cern.molr.commons.api.response.MissionEvent;
import cern.molr.commons.web.SerializationUtils;
import cern.molr.supervisor.api.session.EventsListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;

/**
 * A reader which continually listens for events from a {@link BufferedReader}.
 *
 * @author yassine-kr
 */
public class EventsReader extends RemoteReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventsReader.class);

    private ObjectMapper mapper;
    private EventsListener listener;


    public EventsReader(BufferedReader reader, EventsListener listener) {
        this(reader, DEFAULT_READING_INTERVAL, listener);
    }

    public EventsReader(BufferedReader reader, Duration readInterval, EventsListener listener) {
        super(reader, readInterval, null);
        mapper = SerializationUtils.getMapper();
        this.listener = listener;
        start();
    }

    @Override
    protected void readData(BufferedReader reader) {
        try {
            final String line = reader.readLine();
            LOGGER.info("Reading string event from MoleRunner: {}", line);
            try {
                MissionEvent event = mapper.readValue(line, MissionEvent.class);
                listener.onEvent(event);
            } catch (IOException error) {
                LOGGER.error("unable to deserialize a read event [{}]", line, error);
            }

        } catch (IOException error) {
            LOGGER.error("Error while trying to read an event from the MoleRunner", error);
        }
    }

}
