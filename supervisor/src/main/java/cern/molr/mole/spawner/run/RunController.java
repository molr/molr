package cern.molr.mole.spawner.run;

import cern.molr.commons.response.CommandResponse;
import cern.molr.mole.supervisor.*;
import cern.molr.type.Ack;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A controller which controls running of a mission
 * It communicates with JVM using output and input streams
 *
 * @author yassine-kr
 */
public class RunController implements MoleExecutionController,MoleExecutionListener, Closeable {

    private final Set<MoleExecutionListener> listeners=new HashSet<>();
    private final PrintWriter printWriter;
    private final RunEventsReader reader;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Future<?> loggerTask;

    private ObjectMapper mapper=new ObjectMapper();

    /**
     * Event sent by JVM when it verifies the command. Must be volatile (not cached) because it is accessible by two threads in the same time
     * Can't manage multiple commands in the same time
     */
    private volatile MoleExecutionCommandStatus commandStatus =null;

    public RunController(Process process) {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        printWriter=new PrintWriter(process.getOutputStream());
        reader=new RunEventsReader(new BufferedReader(new InputStreamReader(process.getInputStream())),this);

        this.loggerTask = executor.submit(() -> {
            InputStream processError = process.getErrorStream();
            try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(processError))) {
                while (!Thread.interrupted()) {
                    logLine(errorReader);
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        });

    }

    private static void logLine(BufferedReader reader) throws IOException {
        final String line = reader.readLine();
        if (line != null) {
            System.err.println(line);
        }
    }

    @Override
    public void addMoleExecutionListener(MoleExecutionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeMoleExecutionListener(MoleExecutionListener listener) {
        listeners.remove(listener);
    }

    public MoleExecutionCommandResponse start() {
        MoleExecutionCommand command=new RunCommands.Start();
        return sendCommand(command);
    }

    @Override
    public MoleExecutionCommandResponse sendCommand(MoleExecutionCommand command) {
        try {
            printWriter.println(mapper.writeValueAsString(command));
            printWriter.flush();

            while(commandStatus==null){
            }
            if(commandStatus.isAccepted()){
                commandStatus=null;
                return new CommandResponse.CommandResponseSuccess(new Ack("command sent to JVM"));
            }
            else{
                commandStatus=null;
                return new CommandResponse.CommandResponseFailure(new Exception(commandStatus.getReason()));
            }
        } catch (JsonProcessingException e) {
            commandStatus=null;
            e.printStackTrace();
            return new CommandResponse.CommandResponseFailure(e);
        }
    }

    public MoleExecutionCommandResponse terminate() {
        MoleExecutionCommand command=new RunCommands.Terminate();
        return sendCommand(command);
    }

    @Override
    public void onEvent(MoleExecutionEvent event) {
        if(event instanceof MoleExecutionCommandStatus){
            commandStatus = (MoleExecutionCommandStatus) event;
        }

        else
            listeners.forEach((l)->l.onEvent(event));
    }

    @Override
    public void close(){
        loggerTask.cancel(true);
        executor.shutdown();
        printWriter.close();
    }
}
