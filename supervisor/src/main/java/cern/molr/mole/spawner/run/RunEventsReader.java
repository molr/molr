package cern.molr.mole.spawner.run;

import cern.molr.commons.AnnotatedMissionMaterializer;
import cern.molr.inspector.remote.RemoteReader;
import cern.molr.mole.Mole;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;

/**
 * A reader which continually listens for events from a {@link BufferedReader}.
 * @author yassine
 */
public class RunEventsReader extends RemoteReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunEventsReader.class);

    private ObjectMapper mapper=new ObjectMapper();
    private MoleExecutionListener listener;


    public RunEventsReader(BufferedReader reader,MoleExecutionListener listener) {
        this(reader,DEFAULT_READING_INTERVAL,listener);
    }

    public RunEventsReader(BufferedReader reader, Duration readInterval,MoleExecutionListener listener) {
        super(reader, readInterval,null);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        this.listener=listener;
        start();
    }

    @Override
    protected void readCommand(BufferedReader reader) {
        try {
            final String line = reader.readLine();
            MoleExecutionEvent event=mapper.readValue(line,MoleExecutionEvent.class);
            listener.onEvent(event);
            LOGGER.info("Reading string event from JVM: {}", line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
