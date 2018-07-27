package cern.molr.supervisor.impl.session.runner;

import cern.molr.commons.api.request.MissionCommand;
import cern.molr.supervisor.api.session.runner.CommandListener;
import cern.molr.supervisor.impl.session.RemoteReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;

/**
 * A reader which continually reads commands from a {@link BufferedReader}.
 *
 * @author yassine-kr
 */
public class CommandsReader extends RemoteReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandsReader.class);

    private ObjectMapper mapper = new ObjectMapper();
    private CommandListener listener;


    public CommandsReader(BufferedReader reader, CommandListener listener) {
        this(reader, DEFAULT_READING_INTERVAL, listener);

    }

    public CommandsReader(BufferedReader reader, Duration readInterval, CommandListener listener) {
        super(reader, readInterval, null);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.listener = listener;
        start();
    }

    @Override
    protected void readData(BufferedReader reader) {
        try {
            final String line = reader.readLine();
            try {
                MissionCommand command = mapper.readValue(line, MissionCommand.class);
                listener.onCommand(command);
            } catch (IOException error) {
                LOGGER.error("unable to deserialize a read command [{}]", line, error);
            }
        } catch (IOException error) {
            LOGGER.error("Error while trying to read a command from the Supervisor", error);
        }
    }

}
