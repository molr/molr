package cern.molr.mole.spawner.run.jvm;

import cern.molr.inspector.remote.RemoteReader;
import cern.molr.mole.supervisor.MoleCommandListener;
import cern.molr.mole.supervisor.MoleExecutionCommand;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;

/**
 * A reader which continually reads commands from a {@link BufferedReader}.
 * @author yassine
 */
public class RunCommandsReader extends RemoteReader {

    private ObjectMapper mapper=new ObjectMapper();
    private MoleCommandListener listener;


    public RunCommandsReader(BufferedReader reader, MoleCommandListener listener) {
        super(reader);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.listener=listener;
    }

    public RunCommandsReader(BufferedReader reader, Duration readInterval, MoleCommandListener listener) {
        super(reader, readInterval);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        this.listener=listener;
    }

    @Override
    protected void readCommand(BufferedReader reader) {
        //Possible call before initialization of mapper and listener
        //TODO make initialization before launching periodic listener in super class
        if(mapper==null || listener==null)
            return;
        try {
            final String line = reader.readLine();
            MoleExecutionCommand command=mapper.readValue(line,MoleExecutionCommand.class);
            listener.onCommand(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
