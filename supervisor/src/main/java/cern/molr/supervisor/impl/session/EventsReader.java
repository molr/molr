package cern.molr.mole.spawner.run;

import cern.molr.mole.spawner.RemoteReader;
import cern.molr.commons.response.MissionEvent;
import cern.molr.mole.supervisor.EventsListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;

/**
 * A reader which continually listens for events from a {@link BufferedReader}.
 * @author yassine-kr
 */
public class RunEventsReader extends RemoteReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunEventsReader.class);

    private ObjectMapper mapper=new ObjectMapper();
    private EventsListener listener;


    public RunEventsReader(BufferedReader reader,EventsListener listener) {
        this(reader,DEFAULT_READING_INTERVAL,listener);
    }

    public RunEventsReader(BufferedReader reader, Duration readInterval,EventsListener listener) {
        super(reader, readInterval,null);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        this.listener=listener;
        start();
    }

    @Override
    protected void readCommand(BufferedReader reader) {
        try {
            final String line = reader.readLine();
            MissionEvent event=mapper.readValue(line,MissionEvent.class);
            listener.onEvent(event);
            LOGGER.info("Reading string event from JVM: {}", line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
