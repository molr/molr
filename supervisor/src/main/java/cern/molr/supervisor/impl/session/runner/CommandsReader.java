package cern.molr.mole.spawner.run.jvm;

import cern.molr.mole.spawner.RemoteReader;
import cern.molr.mole.supervisor.CommandListener;
import cern.molr.commons.request.MissionCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;

/**
 * A reader which continually reads commands from a {@link BufferedReader}.
 * @author yassine-kr
 */
public class RunCommandsReader extends RemoteReader {

    private ObjectMapper mapper=new ObjectMapper();
    private CommandListener listener;


    public RunCommandsReader(BufferedReader reader, CommandListener listener) {
        this(reader,DEFAULT_READING_INTERVAL,listener);

    }

    public RunCommandsReader(BufferedReader reader, Duration readInterval, CommandListener listener) {
        super(reader, readInterval,null);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.listener=listener;
        start();
    }

    @Override
    protected void readCommand(BufferedReader reader) {
        try {
            final String line = reader.readLine();
            MissionCommand command=mapper.readValue(line,MissionCommand.class);
            listener.onCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
