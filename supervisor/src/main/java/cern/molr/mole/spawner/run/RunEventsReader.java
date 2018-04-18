package cern.molr.mole.spawner.run;

import cern.molr.inspector.remote.RemoteReader;
import cern.molr.mole.Mole;
import cern.molr.mole.supervisor.MoleExecutionEvent;
import cern.molr.mole.supervisor.MoleExecutionListener;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.time.Duration;

/**
 * A reader which continually listens for events from a {@link BufferedReader}.
 * @author yassine
 */
public class RunEventsReader extends RemoteReader {

    private ObjectMapper mapper=new ObjectMapper();
    private MoleExecutionListener listener;


    public RunEventsReader(BufferedReader reader,MoleExecutionListener listener) {
        super(reader);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        this.listener=listener;
    }

    public RunEventsReader(BufferedReader reader, Duration readInterval,MoleExecutionListener listener) {
        super(reader, readInterval);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
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
            MoleExecutionEvent event=mapper.readValue(line,MoleExecutionEvent.class);
            listener.onEvent(event);
            System.out.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
